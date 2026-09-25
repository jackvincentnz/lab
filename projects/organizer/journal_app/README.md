# Journal App

Web app for viewing Organizer journal entries.

See the [Organizer setup guide](../README.md#getting-started) to run the apps and
services together.

## Development

Run these commands from the repository root:

| Task                                    | Command                                                  |
| --------------------------------------- | -------------------------------------------------------- |
| Build and typecheck the app and tests.  | `bazel build //projects/organizer/journal_app/...`       |
| Build the production bundle.            | `bazel build //projects/organizer/journal_app:build`     |
| Run the app.                            | `bazel run //projects/organizer/journal_app`             |
| Watch sources, rebuild and run the app. | `ibazel run //projects/organizer/journal_app`            |
| Run tests once.                         | `bazel test //projects/organizer/journal_app:test_run`   |
| Watch sources, rebuild and rerun tests. | `ibazel run //projects/organizer/journal_app:test_watch` |
| Watch tests with the Vitest UI.         | `ibazel run //projects/organizer/journal_app:test_ui`    |
| Preview the production bundle.          | `bazel run //projects/organizer/journal_app:preview`     |

The dev server listens at `http://localhost:3004/journal/` and proxies `/graphql`
to `http://localhost:4000`. Use `ibazel` during development so source changes are
recompiled before Vite or Vitest reloads them.

From this directory, `pnpm dev`, `pnpm build`, and `pnpm test` run the development,
production build, and test watch commands. Journal has no component tests yet;
its test targets currently allow an empty suite.

See the [frontend coverage guide](../../../tools/bazel/vitest/README.md) for
collecting and checking TypeScript coverage.
