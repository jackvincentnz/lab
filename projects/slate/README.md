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
`~/Library/Application Support/Slate (dev)` on macOS. Quitting the app stops the service.

Run it in prod mode, which keeps data in `~/Library/Application Support/Slate` and listens on port
5197, so it can run beside dev mode:

```zsh
bazel run //projects/slate:prod
```

Install it in prod mode as `/Applications/Slate.app`, where Spotlight finds it. Quit Slate first, and
run the command again to update it:

```zsh
bazel run //projects/slate:install
```

If the installed app cannot start, it shows why in a dialog. When you start Slate with `bazel run`,
it prints why in the terminal. On macOS, the log is `~/Library/Logs/Slate/slate.log` in prod mode and
`~/Library/Logs/Slate (dev)/slate.log` in dev mode.

## Connect an agent

Run the app, then register its MCP endpoint, `http://127.0.0.1:5297/mcp` in dev mode or
`http://127.0.0.1:5197/mcp` in prod mode. For Claude Code:

```zsh
claude mcp add --transport http --scope user slate http://127.0.0.1:5297/mcp
```

Claude Desktop needs the [mcp-remote](https://github.com/punkpeye/mcp-remote) bridge. Add this
entry to `~/Library/Application Support/Claude/claude_desktop_config.json`, then restart Claude
Desktop:

```json
{
  "mcpServers": {
    "slate": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://127.0.0.1:5297/mcp"]
    }
  }
}
```

## Development

| Task                                           | Command                                    |
| ---------------------------------------------- | ------------------------------------------ |
| Build and typecheck everything.                | `bazel build //projects/slate/...`         |
| Run the desktop app in dev mode.               | `bazel run //projects/slate`               |
| Run the desktop app in prod mode.              | `bazel run //projects/slate:prod`          |
| Run the desktop app with hot reload.           | `ibazel run //projects/slate:watch`        |
| Install or update the macOS app.               | `bazel run //projects/slate:install`       |
| Run the local service on its own.              | `bazel run //projects/slate/service`       |
| Run tests once.                                | `bazel test //projects/slate/...`          |
| Run the service's tests once.                  | `bazel test //projects/slate/service:test` |
| Run the desktop app's tests once.              | `bazel test //projects/slate/desktop:test` |
| Launch the desktop app and test it end to end. | `bazel test //projects/slate/e2e`          |

On Linux, the end-to-end test needs `Xvfb`. On Ubuntu, AppArmor must also allow user namespaces for
Electron in your Bazel output base, as the [CI workflow](../../.github/workflows/main.yml) does.

See the [app README](app/README.md) to work on the web UI in a browser, and
[database migrations](docs/migrations.md) to change the database schema.

`bazel run //projects/slate/service` runs the service in dev mode without the web UI. It uses the
same port as `bazel run //projects/slate`, so run one at a time. Set `SLATE_DATA_DIR` or `SLATE_PORT` to override the data folder
or port. The API is under `/api/`, one path per operation:

```zsh
curl -H 'content-type: application/json' -d '{"name":"Plan the week"}' http://127.0.0.1:5297/api/addItem
curl http://127.0.0.1:5297/api/listItems
```

Pass Electron flags after `--`. For example, expose the Chromium debugging protocol, which lets a
tool inspect and drive the real window:

```zsh
bazel run //projects/slate -- --remote-debugging-port=9222
```

## Project map

- `projects/slate/app`: the React web UI.
- `projects/slate/desktop`: the Electron main process, which starts the service and opens the window.
- `projects/slate/service`: the local service, which owns the database and serves the web UI.
