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
`~/Library/Application Support/Day (dev)` on macOS. Quitting the app stops the service. Use `ibazel`
in place of `bazel` to rebuild as sources change, then reload the window with Cmd+R, or Ctrl+R on
Linux. Restart the command after changing `desktop` or `service`.

Run it in prod mode, which keeps data in `~/Library/Application Support/Day` and listens on port
5197, so it can run beside dev mode:

```zsh
bazel run //projects/day:prod
```

## Development

| Task                                            | Command                                    |
| ----------------------------------------------- | ------------------------------------------ |
| Build and typecheck everything.                 | `bazel build //projects/day/...`           |
| Run the desktop app in dev mode.                | `bazel run //projects/day`                 |
| Run the desktop app in prod mode.               | `bazel run //projects/day:prod`            |
| Run the web UI in a browser, reloading on save. | `ibazel run //projects/day/app`            |
| Run the local service on its own.               | `bazel run //projects/day/service`         |
| Run tests once.                                 | `bazel test //projects/day/...`            |
| Run the service's tests once.                   | `bazel test //projects/day/service:test`   |
| Run the desktop app's tests once.               | `bazel test //projects/day/desktop:test`   |
| Watch sources, rebuild and rerun tests.         | `ibazel run //projects/day/app:test_watch` |

The web UI's dev server listens at `http://localhost:5196`; set `DAY_APP_PORT` to change it. It
sends `/api/` requests to the dev mode service on port 5297, or the port in `DAY_PORT`, so start
that first.

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
