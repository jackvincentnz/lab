# Flattened frontend applications

Use `fe_app` from `//tools/bazel:fe.bzl` once in an application's root BUILD
file. See [Mops](../../projects/mops/app/BUILD.bazel) for a complete invocation
and the [ADR](../../docs/adr/flattened-frontend.md) for the rationale.

## Sources and dependencies

Pass recursive, disjoint source lists to `srcs`, `test_srcs`, and optional
`stories`. Production sources compile together in `:src_ts`; tests, setup,
helpers, and fixtures compile together in `:test_ts`. Supply test support files
explicitly, and exclude them and stories from `srcs`. Bazel globs stop at nested
BUILD files: keep generated code in its own package and pass its compiled target
in `deps`. Avoid per-component BUILD files inside an app.

`deps` supplies production dependencies; `test_deps` and `story_deps` add
dependencies for their respective compilations. Include the framework and type
packages the app uses. For React stories, supply `//:node_modules/@storybook/react`
in `story_deps`. `assets` contains imported CSS/images. `data` supplies
`index.html`, `package.json`, `vite.config.ts`, public assets, and any files or
packages imported by that configuration (including PostCSS configuration).

The macro runs Vite and Vitest from the calling package. Keep app-specific
proxies, setup files, test environment and symlink workarounds in the app's Vite
config. Discover compiled `*.test.js`/`*.spec.js`, and point setup files at their
compiled `.js` paths. For coverage, include
`//tools/bazel/vitest:coverage_config` in `data` and configure `bazelCoverage` as
described in the [coverage guide](vitest/README.md). The macro uses the shared
`ts_project` wrapper, retaining SWC maps and coverage-only original sources.

## Targets

Replace `//projects/mops/app` with the application's package:

| Target                               | Purpose                                                    |
| ------------------------------------ | ---------------------------------------------------------- |
| `:app` (the supplied `name`), `:dev` | Vite development server.                                   |
| `:build`, `:preview`                 | Production bundle and its preview server.                  |
| `:src`                               | Compiled production sources and imported assets.           |
| `:test_run`                          | Cacheable `bazel test` / `bazel coverage` entry point.     |
| `:test_watch`                        | `bazel run` or `ibazel run` for Vitest watch mode.         |
| `:test_ui`                           | Vitest watch mode with the browser UI.                     |
| `:stories`                           | Compiled stories, emitted only when `stories` is nonempty. |

`visibility` applies to these entry points. Internal compilation targets remain
private. `test_tags` applies to `:test_run` (Mops preserves `exclusive`).
Mops retains `:test` as an alias for `:test_watch`.

Stories are a compiled library for an app-owned Storybook configuration, matching
the existing library convention. They do not enter the production bundle or test
runtime. Use the existing `storybook_build` / `storybook_dev_server` macros to
host them when needed; `fe_app` does not generate Storybook configuration or a
server. An empty story glob creates no `:stories` target and no Storybook
compilation dependency.

## Existing libraries

`fe_library` remains compatible with existing Organizer and Bubbles consumers.
It is a legacy directory-level library macro, not the pattern for new apps.
Organizer and Bubbles migrations remain in [#844](https://github.com/jackvincentnz/lab/issues/844)
and [#847](https://github.com/jackvincentnz/lab/issues/847); shared test utilities
and configuration remain in [#845](https://github.com/jackvincentnz/lab/issues/845).
