# Day

A personal desktop app for keeping one ordered list of everything you are working on. It is an
[Electron](https://www.electronjs.org) app with a React window. See the [goals](docs/goals.md) for
what it is for and the [architecture](docs/architecture.md) for how it is put together.

## Getting started

Day is a pnpm workspace of its own, outside Bazel. Follow the
[monorepo setup](../../README.md#getting-started) for pnpm, then install dependencies, which also
downloads the Electron binary:

```zsh
cd projects/day
pnpm install
```

Run the app, reloading the window as sources change:

```zsh
pnpm dev
```

## Development

Run these commands from `projects/day`:

| Task                              | Command          |
| --------------------------------- | ---------------- |
| Run the app and reload on change. | `pnpm dev`       |
| Typecheck.                        | `pnpm typecheck` |
| Build into `dist`.                | `pnpm build`     |
| Run the built app.                | `pnpm start`     |

## Project map

- `src/main`: the Electron main process, which opens the window.
- `src/renderer`: the React window.

## Related docs

- [Goals](docs/goals.md).
- [Architecture](docs/architecture.md).
