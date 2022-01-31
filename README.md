# Lab

<p>
    <a href="https://github.com/allocadia-jack/lab/actions?query=workflow%3ABuild+branch%3Amaster+">
        <img alt="Build status" src="https://github.com/allocadia-jack/lab/actions/workflows/main.yml/badge.svg">
    </a>
</p>

Monorepo for the experimentation of all things Bazel, Javascript, Typescript, Angular, Java etc.

## Getting started

Build solution with:

```zsh
bazel build //...
```

Test solution with:

```zsh
bazel test //...
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

### Generation

- [Schematics](https://github.com/allocadia-jack/lab/blob/master/tools/schematics/README.md): In progress tooling for generating new Angular projects.

### Continuous integration

- [Build & test](https://github.com/allocadia-jack/lab/actions?query=workflow%3ABuild): The solution is built and tested on each PR and commit to master.
