# Lab

<p>
    <a href="https://github.com/allocadia-jack/lab/actions?query=workflow%3ABuild+branch%3Amaster+">
        <img alt="Build status" src="https://github.com/allocadia-jack/lab/actions/workflows/main.yml/badge.svg">
    </a>
    <a href="https://codecov.io/gh/jackvincentnz/lab" >
        <img src="https://codecov.io/gh/jackvincentnz/lab/graph/badge.svg?token=6NY99RW8Z8"/>
    </a>
</p>

Monorepo for the experimentation of all things Bazel, Javascript, Typescript, Angular, Java etc.

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

## Experiments

New projects to be added at `/projects`. Any experiment should have a new project for potential future use in a tutorial.

## Tooling

This repository includes multiple tools to improve consistency, maintainability and developer velocity.

### Build

- [Bazel](https://bazel.build): Fast, multi-language, reproducible, incremental build system.

### Style

- [Prettier](https://prettier.io/docs/en/index.html): Opinionated code formatting.
- [Commitlint](https://commitlint.js.org): Commit message linting.
- [lint-staged](https://github.com/okonet/lint-staged): Pre-commit source linting.
- [ESLint](https://eslint.org/): Find and fix problems in your JavaScript code.

### Generation

- [Schematics](https://github.com/allocadia-jack/lab/blob/master/tools/schematics/README.md): In progress tooling for generating new Angular projects.

### Continuous integration

- [Build & test](https://github.com/allocadia-jack/lab/actions?query=workflow%3ABuild): The solution is built and tested on each PR and commit to master.
