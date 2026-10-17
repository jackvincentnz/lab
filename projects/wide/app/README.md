# WIDE App

Read-only explorer for the WIDE project: browse saved signals, problems, candidate
solutions, questions, decisions, and strategy through the service's GraphQL API.

See the [WIDE setup guide](../README.md#getting-started) to run the app, service, and
database together.

## Development

Run these commands from the repository root:

| Task                                    | Command                                     |
| --------------------------------------- | ------------------------------------------- |
| Build and typecheck the app and tests.  | `bazel build //projects/wide/app/...`       |
| Build the production bundle.            | `bazel build //projects/wide/app:build`     |
| Run the app.                            | `bazel run //projects/wide/app`             |
| Watch sources, rebuild and run the app. | `ibazel run //projects/wide/app`            |
| Run tests once.                         | `bazel test //projects/wide/app:test_run`   |
| Watch sources, rebuild and rerun tests. | `ibazel run //projects/wide/app:test_watch` |
| Watch tests with the Vitest UI.         | `ibazel run //projects/wide/app:test_ui`    |
| Preview the production bundle.          | `bazel run //projects/wide/app:preview`     |

The dev server listens at `http://localhost:5195` and proxies `/api/graphql` to the
service at `http://127.0.0.1:8095`; set `WIDE_PORT` to match a service running on
another port. Use `ibazel` during development so source changes are recompiled before
Vite or Vitest reloads them.

GraphQL documents live in `.gql` files next to their consumers. Types and typed
documents are generated from the service schema into `src/__generated__` as part of
the build, so edit the `.gql` files rather than the generated output.

See the [frontend coverage guide](../../../tools/bazel/vitest/README.md) for
collecting and checking TypeScript coverage.
