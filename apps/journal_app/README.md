# React + TypeScript + Vite

## Summary

This example provides a minimal setup to get React + Vite + HMR working with Bazel.

## Getting started

In the project directory, you can run:

### Development server

`pnpm dev` or `ibazel run //apps/journal_app`

Runs the app in the development mode.
Open http://127.0.0.1:5173/ to view it in the browser.
The page will reload if you make edits.

### Production build

`pnpm build` or `bazel build //apps/journal_app:build`

Builds the app for production to `dist/bin/apps/journal_app/dist`.

## Improvements

- Transpile ts prior with swc
- Merge react ts configs
- Tests
