# Contributing

## Setup

Follow the [getting started guide](README.md#getting-started), then complete
[pre-commit setup](docs/pre-commit.md#setup) before making your first commit.

## Making a change

Keep each PR focused on one problem. Use conventional commit messages, such as
`fix(mops): handle missing budgets` or `docs: clarify local setup`. See
[commit types](#commit-types) and the [style guide](docs/style.md) for commit
and source formatting conventions.

Follow the conventions of the code around your change, so that each project
stays consistent. A project may choose its own architectural style within its
boundary.

Write comments that explain why the code is the way it is, so that they stay
true as plans change. Keep sequencing such as "once #123 lands" in issues.

## Validating a change

- Run `bazel test //projects/<name>/...` for the project you changed, so that
  every target in it is covered, not only the ones you touched.
- Run `bazel test //...` when you change `libs/`, `tools/`, Bazel configuration,
  or dependencies, so that consumers in other projects are covered.
- Run `aspect gazelle` when you add, move, or delete Java files or change
  imports, so that [Java BUILD files](docs/contributing/bazel.md#java-build-files)
  match the code before CI checks them.
- Run `pre-commit run --files <changed files>`, so that formatting and lint
  failures are caught before CI.
- Run the app or service when you change its behavior, so that problems tests
  miss, such as a broken page, are caught before review.

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
- [TypeScript performance](docs/contributing/typescript.md).
- [Testing](docs/contributing/testing.md).
- [Dependencies](docs/contributing/dependencies.md).
- [Markdown](docs/contributing/markdown.md).

## Pull requests and CI

Open PRs against `main`. Fill in only the template's `Change` and `Validation`
sections: a short description of the problem and the resulting behavior, then
the checks you ran, including any failures or checks you could not run. Link the
relevant issue; use `Closes #123` when merging the PR will fully resolve it.
Link repository files with absolute
`https://github.com/jackvincentnz/lab/blob/<branch>/<path>` URLs, so that the
links resolve in the PR description.

Address review feedback and ensure all checks pass on the latest revision before
merging. If a check fails, inspect its log and fix the cause. See the
[workflow definitions](https://github.com/jackvincentnz/lab/tree/main/.github/workflows)
for the current CI configuration.
