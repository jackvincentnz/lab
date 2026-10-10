#!/usr/bin/env python3
"""Read-only feedback on changed lines from an existing Bazel LCOV report."""

import argparse
from pathlib import Path
import re
import subprocess
import sys


def git(root, *args):
    return subprocess.check_output(
        ["git", "-C", str(root), *args], stderr=subprocess.PIPE
    )


def changed_lines(root, base):
    """Compare the current tree (including staged and untracked files) to base."""
    names = git(
        root, "diff", "--no-ext-diff", "--no-textconv", "--no-renames",
        "--name-only", "-z", base, "--",
    ).split(b"\0")
    untracked = git(root, "ls-files", "--others", "--exclude-standard", "-z").split(b"\0")
    result = {}
    for raw in sorted(set(names + untracked) - {b""}):
        name = raw.decode("utf-8", errors="surrogateescape")
        path = root / name
        if raw in untracked:
            content = (
                path.read_bytes() if path.is_file() and not path.is_symlink() else b"\0"
            )
            count = content.count(b"\n") + int(bool(content) and not content.endswith(b"\n"))
            result[name] = (
                set(range(1, count + 1)) if b"\0" not in content else set()
            )
            continue
        diff = git(
            root, "diff", "--no-ext-diff", "--no-textconv", "--no-renames",
            "--unified=0", base, "--", ":(literal)" + name,
        )
        lines = set()
        for start, count in re.findall(
            rb"^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@", diff, re.M
        ):
            first = int(start)
            lines.update(range(first, first + (int(count) if count else 1)))
        result[name] = lines
    return result


def read_lcov(path, root):
    """Merge DA records, keeping empty SF records distinct from measured files."""
    coverage = {}
    source = None
    records = 0
    with path.open(encoding="utf-8", errors="surrogateescape") as report:
        for number, raw in enumerate(report, 1):
            row = raw.rstrip("\r\n")
            if row.startswith("SF:"):
                if source is not None or not row[3:]:
                    raise ValueError(f"invalid SF record at LCOV line {number}")
                source = row[3:]
                resolved = (root / source).resolve()
                try:
                    name = resolved.relative_to(root.resolve()).as_posix()
                except ValueError:
                    name = None
                if name is not None:
                    coverage.setdefault(name, {})
            elif row.startswith("DA:"):
                fields = row[3:].split(",")
                if source is None or len(fields) not in (2, 3):
                    raise ValueError(f"invalid DA record at LCOV line {number}")
                line, hits = int(fields[0]), int(fields[1])
                if line < 1 or hits < 0:
                    raise ValueError(f"invalid DA counts at LCOV line {number}")
                if name is not None:
                    measured = coverage[name]
                    measured[line] = measured.get(line, 0) + hits
            elif row == "end_of_record":
                if source is None:
                    raise ValueError(f"end_of_record without SF at LCOV line {number}")
                source = None
                records += 1
    if source is not None:
        raise ValueError("unterminated LCOV record")
    if not records:
        raise ValueError("LCOV report contains no source records")
    return coverage


def line_ranges(lines):
    groups = []
    for line in sorted(lines):
        if groups and groups[-1][1] + 1 == line:
            groups[-1][1] = line
        else:
            groups.append([line, line])
    return ", ".join(str(a) if a == b else f"{a}-{b}" for a, b in groups) or "none"


def feedback(changes, coverage):
    unknown = False
    for name, changed in sorted(changes.items()):
        print(f"\n{name!r}")
        if not changed:
            print("  No added/modified text lines (deletion, binary, or metadata-only change).")
            continue
        measured = coverage.get(name)
        if not measured:
            unknown = True
            reason = (
                "empty source record; no DA lines" if name in coverage else "no source record"
            )
            print(f"  UNKNOWN: {reason}; changed lines: {line_ranges(changed)}")
            continue
        covered = {line for line in changed if measured.get(line, 0) > 0}
        missed = {line for line in changed if line in measured and measured[line] == 0}
        non_executable = changed - measured.keys()
        print(f"  Covered executable lines: {line_ranges(covered)}")
        print(f"  MISSED executable lines: {line_ranges(missed)}")
        print(f"  Non-executable according to LCOV: {line_ranges(non_executable)}")
    return int(unknown)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--lcov", default="dist/out/_coverage/_coverage_report.dat",
        help="LCOV path, relative to repository root by default",
    )
    parser.add_argument(
        "--base", help="compare worktree to merge-base of this ref and HEAD (default: HEAD)",
    )
    args = parser.parse_args(argv)
    try:
        root = Path(git(Path.cwd(), "rev-parse", "--show-toplevel").decode().strip())
        base = "HEAD"
        if args.base:
            base = git(root, "merge-base", args.base, "HEAD").decode().strip()
        changes = changed_lines(root, base)
        if not changes:
            print("No changed files.")
            return 0
        coverage = read_lcov(root / args.lcov, root)
        print(f"LCOV: {args.lcov}; comparing current files to {base}")
        print("Assumes coverage was collected for the current source snapshot; regenerate after edits.")
        result = feedback(changes, coverage)
        print("\nRead-only feedback; misses are advisory. UNKNOWN is not successful coverage.")
        return result
    except (OSError, ValueError, subprocess.CalledProcessError) as error:
        detail = (
            error.stderr.decode(errors="replace").strip()
            if isinstance(error, subprocess.CalledProcessError) else str(error)
        )
        print(f"Coverage feedback unavailable: {detail}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
