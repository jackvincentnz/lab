# Slate

A personal desktop app for keeping one ordered list of everything you are working on. It is an
[Electron](https://www.electronjs.org) app with a React web UI. See the [goals](docs/goals.md) for
what it is for and the [local service decision](../../docs/adr/slate-local-service.md) for the
design it follows.

## Getting started

Follow the [monorepo setup](../../README.md#getting-started). Run these commands from the
repository root.

Run the desktop app in dev mode:

```zsh
bazel run //projects/slate
```

The first build downloads the Electron binary. The app starts the local service, which serves the
web UI and the API at `http://127.0.0.1:5297` and keeps data in
`~/Library/Application Support/Slate (dev)` on macOS, or in `projects/slate/.dev-data` when run from
a worktree that `git worktree add` created, as [dev data](docs/migrations.md#dev-data) describes. A new data folder starts with sample items. Quitting the app stops the service.

Closing the window leaves the app running in the menu bar, where dev mode's icon reads DEV. Click the
icon to bring the window back, or right-click it and choose Quit Slate.

Started without a terminal, as agents and tests start it, the app runs without a window or a menu
bar icon. Set `SLATE_SHOW_WINDOW=1` to show them.

Run it in prod mode, which keeps data in `~/Library/Application Support/Slate` and listens on port
5197, so it can run beside dev mode:

```zsh
bazel run //projects/slate:prod
```

Install or update it in prod mode as `/Applications/Slate.app`. It starts at login without a window,
so agents can reach the list before you open Slate from the menu bar:

```zsh
bazel run //projects/slate:install
```

## Connect an agent

Run the app in prod mode or install it, then register its MCP endpoint, `http://127.0.0.1:5197/mcp`.
For Claude Code:

```zsh
claude mcp add --transport http --scope user slate http://127.0.0.1:5197/mcp
```

For Claude Desktop, build the Slate extension and open it, then choose Install in the dialog that
Claude Desktop shows:

```zsh
bazel build //projects/slate/extension:slate
open dist/bin/projects/slate/extension/slate.mcpb
```

To connect an agent to the dev mode list instead, register `http://127.0.0.1:5297/mcp` under another
name, such as `slate-dev`. For Claude Desktop, build and open `//projects/slate/extension:slate_dev`,
which is `slate-dev.mcpb`.

## Development

| Task                                           | Command                                   |
| ---------------------------------------------- | ----------------------------------------- |
| Build and typecheck everything.                | `bazel build //projects/slate/...`        |
| Run the desktop app with hot reload.           | `ibazel run //projects/slate:watch`       |
| Run the local service on its own.              | `bazel run //projects/slate/service`      |
| Replace the dev data with sample items.        | `bazel run //projects/slate/service:seed` |
| Run tests once.                                | `bazel test //projects/slate/...`         |
| Launch the desktop app and test it end to end. | `bazel test //projects/slate/e2e`         |

On Linux, the end-to-end test needs `Xvfb`. On Ubuntu, AppArmor must also allow user namespaces for
Electron in your Bazel output base, as the [CI workflow](../../.github/workflows/main.yml) does.

See the [app README](app/README.md) to work on the web UI in a browser,
[database migrations](docs/migrations.md) to change the database schema, and
[troubleshooting](docs/troubleshooting.md) when Slate cannot start.

The service on its own uses the same port and data folder as dev mode, so run one at a time or set
`SLATE_PORT` and `SLATE_DATA_DIR`.

Pass Electron flags after `--`. For example, expose the Chromium debugging protocol, which lets a
tool inspect and drive the real window:

```zsh
bazel run //projects/slate -- --remote-debugging-port=9222
```

## Project map

- `projects/slate/app`: the React web UI.
- `projects/slate/desktop`: the Electron main process, which starts the service and opens the window.
- `projects/slate/extension`: the Claude Desktop extension, which forwards MCP over stdio to the
  service.
- `projects/slate/launcher`: starts Electron for `bazel run` and stops it when `bazel run` stops.
- `projects/slate/service`: the local service, which owns the database and serves the web UI.
