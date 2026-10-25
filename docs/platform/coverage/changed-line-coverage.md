# Changed-line coverage

[diff-cover](https://github.com/Bachmann1234/diff_cover) reports which changed
lines the latest coverage report misses. It compares the report with your
changes since `origin/main`, including committed, staged, unstaged, and
untracked changes. Collect coverage for the tests of the code you changed, then
run the manual [pre-commit](../tools/pre-commit.md) hook:

```sh
bazel coverage --combined_report=lcov //libs/ddd/...
pre-commit run diff-cover --hook-stage manual --all-files
```

Keep `--all-files`: without it, pre-commit stashes unstaged changes before the
hook runs. diff-cover lists only files that the report contains, so a changed
file outside the instrumented targets does not appear. Rerun coverage after you
edit, so that line numbers match the report.
