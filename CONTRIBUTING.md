# Contributing

## Setup

Follow the [getting started guide](README.md#getting-started) to install Bazelisk,
set up the repository's development tools, and install Docker. Project READMEs
describe how to run individual applications. See [development tools](docs/tools.md)
for the Bazel-managed tools and shared pnpm version.

Install `pre-commit`, then install the repository's hooks:

```sh
pre-commit install
```

The [hook configuration](.pre-commit-config.yaml) installs both `pre-commit` and
`commit-msg` hooks. It runs formatters and linters on staged files and checks
commit messages with Commitlint. If a formatter changes files, review and stage
the changes before committing again.

## Making a change

Keep each PR focused on one problem. Use conventional commit messages, such as
`fix(mops): handle missing budgets` or `docs: clarify local setup`. See the
[style guide](docs/style.md) for commit and source formatting conventions.

Run the relevant Bazel build and test targets while working. For repository-wide
validation, use:

```sh
bazel build //...
bazel test //... --test_output=errors
pre-commit run --all-files
```

Tests tagged `requires-docker` need a running Docker engine. In an environment
without Docker, `bazel test --config=codex-cloud //... --test_output=errors`
skips those tests; mention that omission in the PR's validation results.

## Pull requests and CI

Open PRs against `main`. Describe the problem, the resulting behavior, and the
checks you ran, including any failures or checks you could not run. Link the
relevant issue; use `Closes #123` when merging the PR will fully resolve it.

The [Build workflow](.github/workflows/main.yml) runs on PRs targeting `main`
and pushes to `main`:

- `common_ci` runs tests tagged `no-coverage` separately, then runs coverage for
  the remaining tests and uploads the report to Codecov.
- `pull_request_ci` runs the pre-commit formatting and lint checks on PRs.
- After a merge, the push workflow also runs artifact delivery from `main`.

Address review feedback and check the latest CI results before merging. If a
check fails, inspect its log and explain any remaining limitation in the PR.
