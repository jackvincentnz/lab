# Local coverage feedback

Collect coverage for the tests relevant to your current work, then read feedback
on changed lines from the existing Bazel LCOV report. Run from the repository root
with Python 3 and Git on `PATH`:

```sh
bazel coverage --combined_report=lcov //projects/mops/app:test_run //projects/organizer/tasklist:test_run
python3 tools/local_coverage/feedback.py
```

Use Java test targets or `//...` in the same coverage command when appropriate.
The command reads `dist/out/_coverage/_coverage_report.dat` by default. It does
not run tests, change files, or modify normal builds and tests.

By default, feedback compares tracked files to `HEAD`, including staged and
unstaged edits, and includes untracked files that Git does not ignore. For all
work on a branch, including committed changes, use the merge-base with `main`:

```sh
python3 tools/local_coverage/feedback.py --base origin/main
```

To read a specific report, including an individual test's native LCOV:

```sh
python3 tools/local_coverage/feedback.py --lcov dist/testlogs/projects/mops/app/test_run/coverage.dat
```

`--lcov` accepts an absolute path or a path relative to the repository root.
`--base` accepts a Git ref. The script can also be invoked from a subdirectory;
both changed file names and relative report paths remain repository-relative.

## Reading the feedback

For each changed file, the command reports the added or modified current lines:

- **Covered executable lines** have positive LCOV `DA` hit counts.
- **MISSED executable lines** have explicit zero-hit `DA` records.
- **Non-executable according to LCOV** are changed lines omitted from a file
  that has measured `DA` records, such as comments, types, or blank lines.
- **UNKNOWN** means the file has no source record or only an empty record without
  `DA` lines. Empty Bazel baseline records (`LF:0`) are not evidence of coverage.
- Deletions, binary files, and metadata-only changes have no added or modified
  text lines to evaluate.

For example, a measured file might report covered line `1`, missed line `2`, and
non-executable line `3`. A new file absent from the report instead shows `UNKNOWN`
and lists all its changed lines. The command does not guess executability from
file extensions, so documentation and configuration absent from LCOV also show
`UNKNOWN`.

Feedback assumes the report matches the current source snapshot. Regenerate
coverage after edits: LCOV contains line numbers and hits, but cannot reliably
prove freshness or identify incomplete instrumentation within a measured file.
“Non-executable according to LCOV” describes what that report measures, rather
than proving the language cannot execute the line. Choose tests and Bazel's
instrumentation scope that cover the files you are changing. An unrelated test
run can overwrite the combined report with a narrower one. Rename destinations
are evaluated as added files; only their current paths match LCOV.

Exit status is `1` for unknown coverage, a missing/empty/malformed report, or a
Git/read error. Status `0` means the report was readable and no changed text file
was unknown (or there were no changes). It does **not** mean all lines were
covered: missed lines remain advisory. There is no threshold or CI enforcement.

## Validation

Synthetic LCOV fixtures and temporary Git repositories test staged, unstaged,
untracked, committed, renamed, deleted, binary, and metadata-only changes, along
with unknown and malformed data:

```sh
bazel test //tools/local_coverage:feedback_test
```

The test uses host Python 3 and Git, which must be on `PATH`. It is tagged
`no-coverage` because it verifies a reporting tool rather than instrumented
application code.
