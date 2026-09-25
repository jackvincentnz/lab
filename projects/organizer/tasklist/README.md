# Tasklist

Web app for creating and completing Organizer tasks.

See the [Organizer setup guide](../README.md#getting-started) to run the apps and
services together.

## Development

Run these commands from the repository root:

| Task                                    | Command                                               |
| --------------------------------------- | ----------------------------------------------------- |
| Build and typecheck the app and tests.  | `bazel build //projects/organizer/tasklist/...`       |
| Build the production bundle.            | `bazel build //projects/organizer/tasklist:build`     |
| Run the app.                            | `bazel run //projects/organizer/tasklist`             |
| Watch sources, rebuild and run the app. | `ibazel run //projects/organizer/tasklist`            |
| Run tests once.                         | `bazel test //projects/organizer/tasklist:test_run`   |
| Watch sources, rebuild and rerun tests. | `ibazel run //projects/organizer/tasklist:test_watch` |
| Watch tests with the Vitest UI.         | `ibazel run //projects/organizer/tasklist:test_ui`    |
| Preview the production bundle.          | `bazel run //projects/organizer/tasklist:preview`     |

The dev server listens at `http://localhost:3000/task/` and proxies `/graphql`
to `http://localhost:4000`. Use `ibazel` during development so source changes are
recompiled before Vite or Vitest reloads them.

From this directory, `pnpm dev`, `pnpm build`, and `pnpm test` run the development,
production build, and test watch commands. `pnpm start` also starts development.

See the [frontend coverage guide](../../../tools/bazel/vitest/README.md) for
collecting and checking TypeScript coverage.
