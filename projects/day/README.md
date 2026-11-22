# Day

A personal desktop app for keeping one ordered list of everything you are working on. It is an
[Electron](https://www.electronjs.org) app with a React window. See the [goals](docs/goals.md) for
what it is for and the [local service decision](../../docs/adr/day-local-service.md) for the
design it follows.

## Getting started

Follow the [monorepo setup](../../README.md#getting-started). Run these commands from the
repository root.

Run the desktop app:

```zsh
bazel run //projects/day
```

The first build downloads the Electron binary. The window shows the built page, so use `ibazel` in
place of `bazel` to rebuild as sources change, then reload the window with Cmd+R, or Ctrl+R on Linux. Restart the
command after changing `desktop`.

## Development

| Task                                            | Command                                    |
| ----------------------------------------------- | ------------------------------------------ |
| Build and typecheck everything.                 | `bazel build //projects/day/...`           |
| Run the desktop app.                            | `bazel run //projects/day`                 |
| Run the window in a browser, reloading on save. | `ibazel run //projects/day/app`            |
| Run tests once.                                 | `bazel test //projects/day/...`            |
| Watch sources, rebuild and rerun tests.         | `ibazel run //projects/day/app:test_watch` |

The window's dev server listens at `http://localhost:5196`; set `DAY_APP_PORT` to change it. The
window is an ordinary web page, so a browser is the quickest place to work on it.

Pass Electron flags after `--`. For example, expose the Chromium debugging protocol, which lets a
tool inspect and drive the real window:

```zsh
bazel run //projects/day -- --remote-debugging-port=9222
```

## Project map

- `projects/day/app`: the React window.
- `projects/day/desktop`: the Electron main process, which opens the window.
