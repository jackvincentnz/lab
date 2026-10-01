---
name: validate
description: Use when validating a change, before reporting it as done, and before opening or updating a pull request.
---

# Validate

Choose the scope from the changed paths, as
[validating a change](../../../CONTRIBUTING.md#validating-a-change) describes.

1. List the changed files with `git diff origin/main --name-only` and
   `git ls-files --others --exclude-standard`.
2. Map the paths to commands:
   - `projects/<name>/`: `bazel test //projects/<name>/...` for each changed
     project.
   - `libs/`, `tools/`, Bazel configuration, or dependency manifests and
     lockfiles: `bazel test //...` instead of the project commands.
   - All changed files: `pre-commit run --files <changed files>`.
3. Run each command as written, one Bazel command at a time. Do not narrow the
   scope to the targets you touched, and do not add flags, except
   `--config=codex-cloud` when Docker is not available.
4. Fix every failure in the scope, including `ts_typecheck_test` and lint
   targets, then run the same commands again.
5. Run the app or service when the change affects its behavior.

Report each command with its result and counts, for example
`bazel test //projects/wide/...: 48 of 48 tests pass` or
`pre-commit: 9 hooks passed, 4 skipped`. Name each failure and each check that
you did not run.
