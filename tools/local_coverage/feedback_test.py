"""Synthetic LCOV and temporary Git repositories exercise the local command."""

from contextlib import redirect_stdout
import io
import os
from pathlib import Path
import subprocess
import tempfile
import unittest

import feedback


class FeedbackTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name).resolve()
        self.git("init", "-q")
        self.git("config", "user.name", "Synthetic Test")
        self.git("config", "user.email", "test@example.invalid")
        self.write("source.ts", "const a = 1;\nconst b = 2;\n// comment\n")
        self.commit()

    def git(self, *args):
        return subprocess.check_output(["git", "-C", str(self.root), *args], stderr=subprocess.PIPE)

    def write(self, name, text):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text)
        return path

    def commit(self):
        self.git("add", "--all")
        self.git("commit", "-qm", "synthetic fixture")

    def lcov(self, text):
        return self.write(".git/report.dat", text)

    def run_command(self, *args):
        result = subprocess.run(
            ["python3", str(Path(feedback.__file__).resolve()), *args],
            cwd=self.root, capture_output=True, text=True,
        )
        return result.returncode, result.stdout, result.stderr

    def test_staged_unstaged_and_untracked_lines(self):
        self.write("source.ts", "const a = 4;\nconst b = 2;\n// comment\n")
        self.git("add", "source.ts")
        self.write("source.ts", "const a = 4;\nconst b = 5;\n// comment\n")
        self.write("new file.ts", "new\nline\n")
        self.assertEqual(feedback.changed_lines(self.root, "HEAD"), {
            "source.ts": {1, 2}, "new file.ts": {1, 2},
        })

    def test_base_includes_committed_work_and_worktree(self):
        self.git("branch", "base")
        self.write("source.ts", "const a = 4;\nconst b = 2;\n// comment\n")
        self.commit()
        self.write("source.ts", "const a = 4;\nconst b = 5;\n// comment\n")
        report = self.lcov("SF:source.ts\nDA:1,1\nDA:2,0\nend_of_record\n")
        code, out, err = self.run_command("--base", "base", "--lcov", str(report))
        self.assertEqual((code, err), (0, ""))
        self.assertIn("Covered executable lines: 1", out)
        self.assertIn("MISSED executable lines: 2", out)

    def test_merge_base_excludes_changes_only_on_base_branch(self):
        self.git("branch", "work")
        self.write("other.ts", "upstream\n")
        self.commit()
        self.git("branch", "base")
        self.git("checkout", "-q", "work")
        report = self.lcov("SF:source.ts\nDA:1,1\nend_of_record\n")
        code, out, err = self.run_command("--base", "base", "--lcov", str(report))
        self.assertEqual((code, out, err), (0, "No changed files.\n", ""))

    def test_renamed_file_has_new_path_and_deleted_file_has_no_lines(self):
        self.git("mv", "source.ts", "renamed.ts")
        self.assertEqual(feedback.changed_lines(self.root, "HEAD"), {
            "source.ts": set(), "renamed.ts": {1, 2, 3},
        })

    def test_binary_and_metadata_changes_do_not_claim_coverage(self):
        (self.root / "binary").write_bytes(b"\0abc")
        self.commit()
        (self.root / "binary").write_bytes(b"\0def")
        (self.root / "new binary").write_bytes(b"\0def")
        (self.root / "source.ts").chmod(0o755)
        changes = feedback.changed_lines(self.root, "HEAD")
        self.assertEqual(changes, {"binary": set(), "new binary": set(), "source.ts": set()})
        output = io.StringIO()
        with redirect_stdout(output):
            self.assertEqual(feedback.feedback(changes, {}), 0)
        self.assertNotIn("Covered executable", output.getvalue())

    def test_literal_paths_with_spaces_unicode_and_pathspec_characters(self):
        name = "space ü [x].ts"
        self.write(name, "old\n")
        self.commit()
        self.write(name, "new\n")
        self.assertEqual(feedback.changed_lines(self.root, "HEAD"), {name: {1}})

    def test_untracked_line_numbers_follow_git_newlines(self):
        self.write("new.ts", "first\vstill first\nsecond")
        self.assertEqual(feedback.changed_lines(self.root, "HEAD"), {"new.ts": {1, 2}})

    def test_deleting_text_lines_keeps_no_current_lines(self):
        self.write("source.ts", "const a = 1;\nconst b = 2;\n")
        self.assertEqual(feedback.changed_lines(self.root, "HEAD"), {"source.ts": set()})

    def test_ignored_files_are_not_reported(self):
        self.write(".gitignore", "ignored.ts\n")
        self.commit()
        self.write("ignored.ts", "ignored\n")
        self.assertEqual(feedback.changed_lines(self.root, "HEAD"), {})

    def test_covered_missed_and_non_executable_are_separate(self):
        report = self.lcov("SF:source.ts\nDA:1,3\nDA:2,0\nend_of_record\n")
        coverage = feedback.read_lcov(report, self.root)
        output = io.StringIO()
        with redirect_stdout(output):
            code = feedback.feedback({"source.ts": {1, 2, 3}}, coverage)
        self.assertEqual(code, 0)
        self.assertIn("Covered executable lines: 1", output.getvalue())
        self.assertIn("MISSED executable lines: 2", output.getvalue())
        self.assertIn("Non-executable according to LCOV: 3", output.getvalue())

    def test_absent_and_empty_records_are_unknown(self):
        output = io.StringIO()
        with redirect_stdout(output):
            code = feedback.feedback({"absent.ts": {1}, "empty.ts": {2}}, {"empty.ts": {}})
        self.assertEqual(code, 1)
        self.assertIn("UNKNOWN: no source record", output.getvalue())
        self.assertIn("UNKNOWN: empty source record; no DA lines", output.getvalue())
        self.assertNotIn("Covered executable lines", output.getvalue())

    def test_repeated_relative_absolute_and_baseline_records_merge(self):
        report = self.lcov(
            "SF:source.ts\nLF:0\nend_of_record\n"
            "SF:source.ts\nDA:1,0\nDA:2,0,checksum\nend_of_record\n"
            f"SF:{self.root}/source.ts\nDA:1,2\nend_of_record\n"
            "SF:/outside/source.ts\nDA:1,9\nend_of_record\n"
        )
        self.assertEqual(feedback.read_lcov(report, self.root), {"source.ts": {1: 2, 2: 0}})

    def test_invalid_or_empty_reports_fail(self):
        for text in ("", "TN:name\n", "SF:source.ts\nDA:1,0\n", "DA:1,2\n",
                     "SF:source.ts\nDA:0,1\nend_of_record\n",
                     "SF:source.ts\nDA:1,-1\nend_of_record\n",
                     "SF:source.ts\nDA:1,no\nend_of_record\n",
                     "SF:source.ts\nSF:other.ts\nend_of_record\n"):
            with self.subTest(text=text), self.assertRaises(ValueError):
                feedback.read_lcov(self.lcov(text), self.root)

    def test_missing_report_and_invalid_base_fail_without_success(self):
        self.write("source.ts", "changed\n")
        for args in (("--lcov", "missing.dat"), ("--base", "missing-ref")):
            with self.subTest(args=args):
                code, out, err = self.run_command(*args)
                self.assertEqual(code, 1)
                self.assertIn("Coverage feedback unavailable", err)
                self.assertNotIn("Covered executable", out)

    def test_unknown_cli_result_is_nonzero(self):
        self.write("source.ts", "changed\n")
        report = self.lcov("SF:source.ts\nLF:0\nend_of_record\n")
        code, out, err = self.run_command("--lcov", str(report))
        self.assertEqual((code, err), (1, ""))
        self.assertIn("UNKNOWN", out)

    def test_no_changes_needs_no_report(self):
        self.assertEqual(self.run_command(), (0, "No changed files.\n", ""))

    def test_subdirectory_invocation_uses_repository_relative_paths(self):
        self.write("source.ts", "changed\n")
        self.lcov("SF:source.ts\nDA:1,2\nend_of_record\n")
        subdir = self.root / "subdir"
        subdir.mkdir()
        previous = Path.cwd()
        try:
            os.chdir(subdir)
            with redirect_stdout(io.StringIO()) as output:
                self.assertEqual(feedback.main(["--lcov", ".git/report.dat"]), 0)
            self.assertIn("Covered executable lines: 1", output.getvalue())
        finally:
            os.chdir(previous)

    def test_line_ranges(self):
        self.assertEqual(feedback.line_ranges({1, 2, 3, 5, 7, 8}), "1-3, 5, 7-8")
        self.assertEqual(feedback.line_ranges(set()), "none")


if __name__ == "__main__":
    unittest.main()
