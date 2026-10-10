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

Each local port has an `ORGANIZER_<COMPONENT>_PORT` variable that overrides it,
for example `ORGANIZER_JOURNAL_PORT=3103 bazel run //projects/organizer/journal/src/main`.
The services, Vite dev servers, and Docker environment read the same variables, so
set them for every component you run. The list of ports and variables is at the top
of [`infra/local/docker-compose.yml`](../../infra/local/docker-compose.yml).

## End-to-end tests

The E2E suite exercises routing and task completion through the delivered apps,
including the resulting journal entry. It requires a running Docker engine.
Run it from the repository root:

```zsh
bazel test //projects/organizer/e2e
```

## Related docs

- [Tasklist](tasklist/README.md).
- [Journal app](journal_app/README.md).
