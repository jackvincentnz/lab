# Mops App

Web app for the Mops project.

See the [Mops setup guide](../README.md#getting-started) to run the app and service
together and configure the service's AI provider.

## Development

Run these commands from the repository root:

| Task                                    | Command                                     |
| --------------------------------------- | ------------------------------------------- |
| Build and typecheck the app and tests.  | `bazel build //projects/mops/app/...`       |
| Build the production bundle.            | `bazel build //projects/mops/app:build`     |
| Run the app.                            | `bazel run //projects/mops/app`             |
| Watch sources, rebuild and run the app. | `ibazel run //projects/mops/app`            |
| Run tests once.                         | `bazel test //projects/mops/app:test_run`   |
| Watch sources, rebuild and rerun tests. | `ibazel run //projects/mops/app:test_watch` |
| Watch tests with the Vitest UI.         | `ibazel run //projects/mops/app:test_ui`    |
| Preview the production bundle.          | `bazel run //projects/mops/app:preview`     |

The dev server listens at `http://localhost:5173/mops/` and proxies `/mops/api` and
`/mops/ws` to the service at `http://localhost:8080`. The `/mops` base lets the
[gateway](../../gateway/README.md) serve the app at `http://localhost:3006/mops/`; the
production bundle keeps base `/`. Use `ibazel` during development so source changes are
recompiled before Vite or Vitest reloads them.

See the [frontend coverage guide](../../../tools/bazel/vitest/README.md) for
collecting and checking TypeScript coverage.
