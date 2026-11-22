# Day App

The web UI of the [Day](../README.md) desktop app: a React page that shows the list in priority
order and lets you add items, rename them and change their status. It calls the local service's
[tRPC](https://trpc.io) API under `/api/` and uses [Mantine](https://mantine.dev) components.

See the [Day setup guide](../README.md#getting-started) to run the desktop app, which builds this
page and serves it from the local service.

## Development

Run these commands from the repository root:

| Task                                    | Command                                    |
| --------------------------------------- | ------------------------------------------ |
| Build and typecheck the app and tests.  | `bazel build //projects/day/app/...`       |
| Build the production bundle.            | `bazel build //projects/day/app:build`     |
| Run the app in a browser.               | `bazel run //projects/day/app`             |
| Watch sources, rebuild and run the app. | `ibazel run //projects/day/app`            |
| Run tests once.                         | `bazel test //projects/day/app:test_run`   |
| Watch sources, rebuild and rerun tests. | `ibazel run //projects/day/app:test_watch` |
| Watch tests with the Vitest UI.         | `ibazel run //projects/day/app:test_ui`    |

The dev server listens at `http://localhost:5196`; set `DAY_APP_PORT` to change it. It sends
`/api/` requests to the dev mode service at `http://127.0.0.1:5297`, or the port in `DAY_PORT`, so
start `bazel run //projects/day/service` or `bazel run //projects/day` first. The page is an
ordinary web page, so a browser is the quickest place to work on it. Use `ibazel` during
development so source changes are recompiled before Vite or Vitest reloads them.

The client takes its types from the service's router with a type-only import, so the bundle holds
no service code. The service's content security policy blocks inline style elements, so the page
loads Mantine's styles from its stylesheet only.

See the [frontend coverage guide](../../../tools/bazel/vitest/README.md) for collecting and
checking TypeScript coverage.
