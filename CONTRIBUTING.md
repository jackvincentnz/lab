# Contributing

## Setup

Follow the [getting started guide](README.md#getting-started), then complete
[pre-commit setup](docs/pre-commit.md#setup) before making your first commit.

## Making a change

Keep each PR focused on one problem. Use conventional commit messages, such as
`fix(mops): handle missing budgets` or `docs: clarify local setup`. See
[commit types](#commit-types) and the [style guide](docs/style.md) for commit
and source formatting conventions.

## Validating a change

Validate every change before you report it or open a PR:

- For changes inside one project, run `bazel test //projects/<name>/...`.
- For changes to `libs/`, `tools/`, `MODULE.bazel`, `.bazelrc`, lockfiles, or
  dependency versions, run `bazel test //...`.
- Run `pre-commit run --files <changed files>`, because Bazel tests do not lint
  or format.
- For changes to a running app or service, start it and check the affected flow
  in a browser or with the client it serves.

Use these commands without extra flags, and run one Bazel command at a time.
For repository-wide validation, use:

```sh
bazel build //...
bazel test //...
pre-commit run --all-files
```

Tests tagged `requires-docker` need a running Docker engine. In an environment
without Docker, `bazel test --config=codex-cloud //...`
skips those tests; mention that omission in the PR's validation results.

## Commit types

Choose the type by the effect of the change, not by the files it touches:

- `feat` adds a user- or developer-facing capability, even when most of the diff
  is BUILD files, lockfiles, or config. For example,
  `feat(gateway): add Spring Cloud Gateway and Spring Session`.
- `fix` corrects behavior. For example,
  `fix(mops): apply tool options on top of defaults`.
- `build` changes the build system or dependency pins without adding a
  capability. For example, `build: wrap Java rules and enable strict globs`.
- `chore` is housekeeping with no build or capability effect. For example,
  `chore: remove Cypress Aspect task`.
- `docs` changes documentation only. For example, `docs: clarify local setup`.

## Contribution guidelines

- [Bazel](docs/contributing/bazel.md).
- [Java](docs/contributing/java.md).
- [Testing](docs/contributing/testing.md).
- [Frontend](docs/contributing/frontend.md).
- [Dependencies](docs/contributing/dependencies.md).
- [READMEs and docs](docs/contributing/readmes.md).

## Pull requests and CI

Open PRs against `main`. Fill in only the template's `Change` and `Validation`
sections: a short description of the problem and the resulting behavior, then
the checks you ran, including any failures or checks you could not run. Link the
relevant issue; use `Closes #123` when merging the PR will fully resolve it.
Link repository files with absolute
`https://github.com/jackvincentnz/lab/blob/<branch>/<path>` URLs, because
relative paths do not resolve in PR descriptions. When the PR makes a
non-obvious design choice, name the alternative and why it was not chosen.

Address review feedback and ensure all checks pass on the latest revision before
merging. If a check fails, inspect its log and fix the cause. See the
[workflow definitions](https://github.com/jackvincentnz/lab/tree/main/.github/workflows)
for the current CI configuration.
