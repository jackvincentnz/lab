# Bubbles

## Summary

A minimal setup to get a component library example.

Bootstrapped by:

- `pnpm create vite bubbles --template react-swc-ts`
- `pnpm dlx storybook@latest init`
- Then Bazelified.

Combines:

- Bazel
- React
- Vite
- Storybook

## Getting started

In this directory, you can run:

### Development server

```shell
pnpm storybook
# or
ibazel run //libs/bubbles:storybook
```

Runs storybook in the development mode.
The page will reload if you make edits.

### Production build

```shell
pnpm build-storybook
# or
bazel build //libs/bubbles:build_storybook
```

Builds storybook for production to `dist/bin/libs/bubbles/storybook-static`.

### Docker image build

```shell
pnpm build-storybook-docker
# or
bazel run //libs/bubbles:tarball
```

Builds and tags storybook docker image which can be run with docker.

### Run with Docker

Depends on building and tagging with `pnpm build-storybook-docker` first.

1. Run

   ```shell
   pnpm run-docker
   # or
   docker run -p 3005:80 lab/bubbles:latest
   ```

2. Open storybook in the browser [http://localhost:3005/](http://localhost:3005/).

## Improvements

- Types
- Tests (snapshot and rapid testing + development example).
- Export and consume library.
