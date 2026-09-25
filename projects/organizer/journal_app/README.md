# Journal

## Development

Run `pnpm dev` from this directory, or `ibazel run //projects/organizer/journal_app`
from the repository root. The explicit `:app` target starts the same server.
Open [http://localhost:3004/journal/](http://localhost:3004/journal/).
The page reloads when you edit source files.

## Build and typecheck

Run `pnpm build` or `bazel build //projects/organizer/journal_app:build` to build
production assets in `dist/bin/projects/organizer/journal_app/dist`.

Typecheck production and test sources with:

```sh
bazel build //projects/organizer/journal_app:src_ts //projects/organizer/journal_app:test_ts
```

The app uses one `fe_app` in the root `BUILD.bazel`. Production sources and tests
compile separately. GraphQL documents are collected by `:gql`, and the
`src/__generated__` package generates and compiles the client code for both.

## Tests and coverage

Run `pnpm test` for watch mode, or use the Bazel targets from the repository root:

```sh
bazel test //projects/organizer/journal_app:test_run
ibazel run //projects/organizer/journal_app:test_ui
bazel coverage --combined_report=lcov //projects/organizer/journal_app:test_run
```

Journal has no component tests yet. Its standard test targets allow an empty
suite; a successful run does not indicate component test coverage.

The Organizer E2E suite exercises the delivered apps and requires Docker:

```sh
bazel test //projects/organizer/e2e:e2e
```
