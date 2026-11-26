# Contributing

## Setup

Follow the [getting started guide](README.md#getting-started), then complete
[pre-commit setup](docs/platform/tools/pre-commit.md#setup) before making your first commit.

## Making a change

Keep a PR to related changes that can land together and that a reviewer can
check in one sitting, so that review stays cheap. Use conventional commit
messages, such as `fix(mops): handle missing budgets` or
`docs: clarify local setup`. See [commit types](#commit-types) and the
[style guide](docs/contributing/style.md) for commit and source formatting
conventions.

To be prompted for each part of a conventional commit instead, stage your
changes and run [Commitizen](https://github.com/commitizen/cz-cli) from the
repository root:

```sh
pnpm install
pnpm commit
```

The pre-commit and Commitlint hooks still check the commit. If a hook fails,
fix and stage the changes, then run `pnpm commit --retry` to reuse the message.

Follow the conventions of the code around your change, so that each project
stays consistent. A project may choose its own architectural style within its
boundary.

Solve a problem with the mechanism the framework or tool documents and the
smallest change that works, so that the code stays familiar and cheap to change.
When you depart from the usual approach, say why in the PR.

Leave out checks for states that your code or the framework cannot produce, and
delete such branches instead of testing them, so that every branch is one a
reader needs.

Specify behavior in tests rather than comments, so that a change that breaks it
fails a test instead of leaving a stale comment. Comment only a why that the
code, its names, and its tests cannot show, never what the code does or how it
came to be. Keep progress notes such as "once #123 lands" in issues.

Put a refactor in its own commit or PR before the behavior change, with test
assertions unchanged, so that a reviewer can check each part on its own.

## Validating a change

- Run `bazel test //projects/<name>/...` for the project you changed before you
  open a PR, so that every target in it is covered, not only the ones you
  touched. Targeted tests are enough while you iterate.
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

Choose the type by the effect of the change, not by the files it touches, so
that the history shows what each commit did:

| Type       | Use when the change                                                                                                                 | Example                                                        |
| ---------- | ----------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------- |
| `feat`     | adds a capability for users of an app or callers of a library, even when most of the diff is BUILD files, lockfiles, or config      | `feat(gateway): add Spring Cloud Gateway and Spring Session`   |
| `fix`      | corrects behavior                                                                                                                   | `fix(mops): apply tool options on top of defaults`             |
| `refactor` | restructures code without changing behavior                                                                                         | `refactor(mops): load the system prompt from a resource`       |
| `perf`     | makes code, builds, or tests faster without changing behavior                                                                       | `perf(vitest): share modules across test files on one worker`  |
| `test`     | adds or changes tests or test helpers, without changing production code                                                             | `test(organizer): add tasklist component tests`                |
| `build`    | changes how code is built, linted, tested, or packaged, locally and in CI, including test runner config and local developer tooling | `build: wrap Java rules and enable strict globs`               |
| `ci`       | changes only how CI runs, such as a workflow or CI-only config                                                                      | `ci: define Codecov coverage policy and components`            |
| `style`    | adds a linter, formatter, or style rule and applies it to existing code, without changing behavior                                  | `style: prefer single imports and exports with explicit types` |
| `docs`     | changes documentation only, including agent guidance and code comments                                                              | `docs: explain coverage tooling and local feedback`            |
| `chore`    | is housekeeping that no other row covers, such as `.gitignore` entries or removing unused files                                     | `chore: remove Cypress Aspect task`                            |
| `revert`   | undoes earlier commits, including with the `Revert "…"` message that `git revert` writes                                            | `revert: wrap Java rules and enable strict globs`              |

When two rows fit, use the more specific one, such as `style` or `perf` over
`build` or `ci`, so that one change always gets the same type. Use the project
or area most affected as the scope, such as `mops`, `bazel`, or `ci`, so that
the scope says where to look.

A dependency update you make takes `fix(deps)` when it fixes a security
vulnerability or something broken, and `chore(deps)` when it only moves to a
newer version, so that a needed fix stands out from routine upgrades. This
includes overrides, explicit versions, and updates to Bazel and toolchains,
which are not `build`. Renovate's PRs keep the type Renovate gives them, which
depends on the kind of dependency.

## Contribution guidelines

- [Bazel](docs/contributing/bazel.md).
- [Java](docs/contributing/java.md).
- [TypeScript](docs/contributing/typescript.md).
- [Testing](docs/contributing/testing.md).
- [Dependencies](docs/contributing/dependencies.md).
- [Markdown](docs/contributing/markdown.md).

## Pull requests and CI

Open PRs against `main`. Title a PR as a conventional commit message that
describes the changes it will merge, so that a squash merge can use the title.
Fill in only the template's `Change` and `Validation` sections: a short
description of the problem and the resulting behavior, then the checks you ran,
including any failures or checks you could not run. Link the relevant issue;
use `Closes #123` when merging the PR will fully resolve it. Link repository
files with absolute `https://github.com/jackvincentnz/lab/blob/<branch>/<path>`
URLs, so that the links resolve in the PR description.

Every commit that lands on `main` is one unit of work that you would revert as a
whole, such as one feature, one fix or one refactor, so that each unit is easy
to understand and revert. Where you can, make each commit build and pass its
tests on its own, so that reverting a later commit leaves `main` working. A PR
that is one unit is squash-merged. A PR with several units is rebase-merged once
its commits are tidied into one per unit. Progress commits are fine while a PR
is in review, so that the reviewer can see what changed since the last review.

Address review feedback and ensure all checks pass on the latest revision before
merging. If a check fails, inspect its log and fix the cause. See the
[workflow definitions](https://github.com/jackvincentnz/lab/tree/main/.github/workflows)
for the current CI configuration.
