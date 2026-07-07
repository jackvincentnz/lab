# Lab

<p>
    <a href="https://github.com/jackvincentnz/lab/actions?query=workflow%3ABuild+branch%3Amain+">
        <img alt="Build status" src="https://github.com/jackvincentnz/lab/actions/workflows/main.yml/badge.svg">
    </a>
    <a href="https://codecov.io/gh/jackvincentnz/lab" >
        <img src="https://codecov.io/gh/jackvincentnz/lab/graph/badge.svg?token=6NY99RW8Z8"/>
    </a>
</p>

Monorepo for experimenting with Bazel, Typescript, Java etc.

## Getting started

Install Bazelisk to automatically use the repo-pinned Bazel version:

```zsh
brew install bazelisk
```

Set up the repository-local development tools:

```zsh
bazel run tools:bazel_env
```

The `tools:bazel_env` target uses
[buildbuddy-io/bazel_env.bzl](https://github.com/buildbuddy-io/bazel_env.bzl)
to build a consistent tool environment for this repo. Follow the tool prompts
to get setup.

Install Docker:

```zsh
brew install --cask docker
```

Build solution with:

```zsh
bazel build //...
```

Test solution with:

```zsh
bazel test //...
```

Project-specific setup and commands live in each project README (for example,
`projects/organizer/README.md`).

## Tooling

This repository includes multiple tools to improve consistency, maintainability and developer efficiency.

### Build

- [Bazel](https://bazel.build): Fast, multi-language, reproducible, incremental build system.
- [bazel_env.bzl](https://github.com/buildbuddy-io/bazel_env.bzl): Enables
  consistent development tools when working in this repo.

### Style

- [pre-commit](https://pre-commit.com/): Pre-commit source linting.
- [Commitlint](https://commitlint.js.org): Commit message linting.
- [Prettier](https://prettier.io/docs/en/index.html): Opinionated code formatting.
- [ESLint](https://eslint.org/): Find and fix problems in your JavaScript code.

More info: [Style](docs/style.md)

### Continuous integration

- [Build & test](https://github.com/jackvincentnz/lab/actions?query=workflow%3ABuild): The solution is built and tested on each PR and commit to main.
