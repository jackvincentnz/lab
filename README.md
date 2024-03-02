# Lab

<p>
    <a href="https://github.com/allocadia-jack/lab/actions?query=workflow%3ABuild+branch%3Amain+">
        <img alt="Build status" src="https://github.com/allocadia-jack/lab/actions/workflows/main.yml/badge.svg">
    </a>
    <a href="https://codecov.io/gh/jackvincentnz/lab" >
        <img src="https://codecov.io/gh/jackvincentnz/lab/graph/badge.svg?token=6NY99RW8Z8"/>
    </a>
</p>

Monorepo for experimenting with Bazel, Typescript, Java etc.

## Getting started

Setup build environment with:

```zsh
brew install bazelisk
brew cask install docker
```

Build solution with:

```zsh
bazel build //...
```

Test solution with:

```zsh
bazel test //...
```

Run the whole local environment in docker with:

```zsh
bazel run //:start
```

Run e2e tests with:

```zsh
bazel test //apps/organizer/e2e
```

## Tooling

This repository includes multiple tools to improve consistency, maintainability and developer efficiency.

### Build

- [Bazel](https://bazel.build): Fast, multi-language, reproducible, incremental build system.

### Style

- [pre-commit](https://pre-commit.com/): Pre-commit source linting.
- [Commitlint](https://commitlint.js.org): Commit message linting.
- [Prettier](https://prettier.io/docs/en/index.html): Opinionated code formatting.
- [ESLint](https://eslint.org/): Find and fix problems in your JavaScript code.

More info: [Style](docs/style.md)

### Continuous integration

- [Build & test](https://github.com/allocadia-jack/lab/actions?query=workflow%3ABuild): The solution is built and tested on each PR and commit to master.
