# Contributing

## Setup

Follow the [getting started guide](README.md#getting-started), then complete
[pre-commit setup](docs/pre-commit.md#setup) before making your first commit.

## Making a change

Keep each PR focused on one problem. Use conventional commit messages, such as
`fix(mops): handle missing budgets` or `docs: clarify local setup`. See the
[style guide](docs/style.md) for commit and source formatting conventions.

Run the relevant Bazel build and test targets while working. For repository-wide
validation, use:

```sh
bazel build //...
bazel test //...
pre-commit run --all-files
```

Tests tagged `requires-docker` need a running Docker engine. In an environment
without Docker, `bazel test --config=codex-cloud //...`
skips those tests; mention that omission in the PR's validation results.

## Pull requests and CI

Open PRs against `main`. Describe the problem, the resulting behavior, and the
checks you ran, including any failures or checks you could not run. Link the
relevant issue; use `Closes #123` when merging the PR will fully resolve it.

Address review feedback and ensure all checks pass on the latest revision before
merging. If a check fails, inspect its log and fix the cause. See the
[workflow definitions](https://github.com/jackvincentnz/lab/tree/main/.github/workflows)
for the current CI configuration.
