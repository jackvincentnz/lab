# Organizer

Services and apps that make up the organizer stack (task, journal, autojournal, tasklist, journal app),
plus the local Docker environment and end-to-end tests.

## Getting started

Build organizer targets with:

```zsh
bazel build //projects/organizer/...
```

Run the whole local environment in Docker (builds and tags images first) with:

```zsh
bazel run //projects/organizer:start
```

Run the local environment using existing images (skips Bazel builds) with:

```zsh
bazel run //projects/organizer:local_environment
```

Run e2e tests with:

```zsh
bazel test //projects/organizer/e2e
```

## Related docs

- `projects/organizer/tasklist/README.md`
- `projects/organizer/journal_app/README.md`
