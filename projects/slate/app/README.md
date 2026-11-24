# Slate App

The web app for [Slate](../README.md). The desktop app renders it in its Electron window, and you
can also open it in a browser while you work on it. See the
[Slate setup guide](../README.md#getting-started) to run the desktop app.

## Development

Run these commands from the repository root:

| Task                                    | Command                                      |
| --------------------------------------- | -------------------------------------------- |
| Build and typecheck the app and tests.  | `bazel build //projects/slate/app/...`       |
| Build the production bundle.            | `bazel build //projects/slate/app:build`     |
| Run the app in a browser.               | `bazel run //projects/slate/app`             |
| Watch sources, rebuild and run the app. | `ibazel run //projects/slate/app`            |
| Run tests once.                         | `bazel test //projects/slate/app:test_run`   |
| Watch sources, rebuild and rerun tests. | `ibazel run //projects/slate/app:test_watch` |
| Watch tests with the Vitest UI.         | `ibazel run //projects/slate/app:test_ui`    |

The dev server listens at `http://localhost:5196`; set `SLATE_APP_PORT` to change it. It sends
`/api/` requests to the dev mode service at `http://127.0.0.1:5297`, or the port in `SLATE_PORT`, so
start `bazel run //projects/slate/service` or `bazel run //projects/slate` first. Use `ibazel` during
development so source changes are recompiled before Vite or Vitest reloads them.

See the [frontend coverage guide](../../../tools/bazel/vitest/README.md) for collecting and
checking TypeScript coverage.
