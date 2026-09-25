# Tasklist

## Development

Run `pnpm dev` from this directory, or `ibazel run //projects/organizer/tasklist`
from the repository root. The explicit `:app` target starts the same server.
Open [http://localhost:3000/task/](http://localhost:3000/task/).
The page reloads when you edit source files.

`pnpm start` remains an alias for `pnpm dev`.

## Build and typecheck

Run `pnpm build` or `bazel build //projects/organizer/tasklist:build` to build
production assets in `dist/bin/projects/organizer/tasklist/dist`.

Typecheck production and test sources with:

```sh
bazel build //projects/organizer/tasklist:src_ts //projects/organizer/tasklist:test_ts
```

The app uses one `fe_app` in the root `BUILD.bazel`. Production sources and tests
compile separately. GraphQL documents are collected by `:gql`, and the
`src/__generated__` package generates and compiles the client code for both.

## Tests and coverage

Run `pnpm test` for watch mode, or use the Bazel targets from the repository root:

```sh
bazel test //projects/organizer/tasklist:test_run
ibazel run //projects/organizer/tasklist:test_ui
bazel coverage --combined_report=lcov //projects/organizer/tasklist:test_run
```

The existing `DisplayTasks` test runs once against compiled JavaScript. Coverage
remaps to source TS/TSX and includes unimported production modules. See the
[frontend coverage guide](../../../tools/bazel/vitest/README.md).

The Organizer E2E suite exercises the delivered apps and requires Docker:

```sh
bazel test //projects/organizer/e2e:e2e
```
