# Day

A personal desktop app for keeping one ordered list of everything you are working on. It is an
[Electron](https://www.electronjs.org) app with a React web UI. See the [goals](docs/goals.md) for
what it is for and the [local service decision](../../docs/adr/day-local-service.md) for the
design it follows.

## Getting started

Follow the [monorepo setup](../../README.md#getting-started). Run these commands from the
repository root.

Run the desktop app in dev mode:

```zsh
bazel run //projects/day
```

The first build downloads the Electron binary. The app starts the local service, which serves the
web UI and the API at `http://127.0.0.1:5297` and keeps data in
`~/Library/Application Support/Day (dev)` on macOS. Quitting the app stops the service.

Run it in prod mode, which keeps data in `~/Library/Application Support/Day` and listens on port
5197, so it can run beside dev mode:

```zsh
bazel run //projects/day:prod
```

Install it in prod mode as `/Applications/Day.app`, where Spotlight finds it. Quit Day first, and
run the command again to update it:

```zsh
bazel run //projects/day:install
```

## Connect an agent

Run the app, then register its MCP endpoint, `http://127.0.0.1:5297/mcp` in dev mode or
`http://127.0.0.1:5197/mcp` in prod mode. For Claude Code:

```zsh
claude mcp add --transport http --scope user day http://127.0.0.1:5297/mcp
```

Claude Desktop needs the [mcp-remote](https://github.com/punkpeye/mcp-remote) bridge. Add this
entry to `~/Library/Application Support/Claude/claude_desktop_config.json`, then restart Claude
Desktop:

```json
{
  "mcpServers": {
    "day": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://127.0.0.1:5297/mcp"]
    }
  }
}
```

## Development

| Task                                           | Command                                  |
| ---------------------------------------------- | ---------------------------------------- |
| Build and typecheck everything.                | `bazel build //projects/day/...`         |
| Run the desktop app in dev mode.               | `bazel run //projects/day`               |
| Run the desktop app in prod mode.              | `bazel run //projects/day:prod`          |
| Run the desktop app with hot reload.           | `ibazel run //projects/day:watch`        |
| Install or update the macOS app.               | `bazel run //projects/day:install`       |
| Run the local service on its own.              | `bazel run //projects/day/service`       |
| Run tests once.                                | `bazel test //projects/day/...`          |
| Run the service's tests once.                  | `bazel test //projects/day/service:test` |
| Run the desktop app's tests once.              | `bazel test //projects/day/desktop:test` |
| Launch the desktop app and test it end to end. | `bazel test //projects/day/e2e`          |

On Linux, the end-to-end test needs `Xvfb`. On Ubuntu, AppArmor must also allow user namespaces for
Electron in your Bazel output base, as the [CI workflow](../../.github/workflows/main.yml) does.

See the [app README](app/README.md) to work on the web UI in a browser.

`bazel run //projects/day/service` runs the service in dev mode without the web UI. It uses the
same port as `bazel run //projects/day`, so run one at a time. Set `DAY_DATA_DIR` or `DAY_PORT` to override the data folder
or port. The API is under `/api/`, one path per operation:

```zsh
curl -H 'content-type: application/json' -d '{"name":"Plan the week"}' http://127.0.0.1:5297/api/addItem
curl http://127.0.0.1:5297/api/listItems
```

Pass Electron flags after `--`. For example, expose the Chromium debugging protocol, which lets a
tool inspect and drive the real window:

```zsh
bazel run //projects/day -- --remote-debugging-port=9222
```

## Project map

- `projects/day/app`: the React web UI.
- `projects/day/desktop`: the Electron main process, which starts the service and opens the window.
- `projects/day/service`: the local service, which owns the database and serves the web UI.
