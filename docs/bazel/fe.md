# Frontend applications

Declare Vite applications.

<!-- Generated with Stardoc: http://skydoc.bazel.build -->

<a id="fe_app"></a>

## fe_app

```starlark
load("//tools/bazel:fe.bzl", "fe_app")

fe_app(name, srcs, deps, assets, data, test_srcs, test_deps, test_shard_count, test_tags, stories, story_deps, visibility)
```

Declare one flattened Vite application per Bazel package.

Emits the named Vite dev server, `src_ts`, `src`, `build`, `preview`, `test_ts`,
`test_run`, `test_watch`, `test_ui` and, when present, `stories`.
By default, discover TypeScript under `src/` in the calling package, separating
production, tests/helpers and stories. Explicit source lists override these
defaults (including `[]`); keep overrides disjoint. Keep app-specific Vite/Vitest
configuration in the calling package; tests consume compiled JS. `test_run`
gets one Bazel shard for every 75 test files once it has more than 75.
Stories compile separately for an app-owned Storybook configuration.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="fe_app-name"></a>name |  Target name for the Vite dev server (normally the package basename).   |  none |
| <a id="fe_app-srcs"></a>srcs |  Production sources; defaults to `src/**/*.ts(x)`, excluding the test/helper and story patterns below.   |  `None` |
| <a id="fe_app-deps"></a>deps |  Libraries imported by production TypeScript (e.g. React or generated GraphQL code). Used for typechecking and included in Vite runfiles.   |  `[]` |
| <a id="fe_app-assets"></a>assets |  Assets imported by production sources.   |  `[]` |
| <a id="fe_app-data"></a>data |  Files and packages consumed by Vite/Vitest at runtime, rather than by application TypeScript compilation: `index.html`, `package.json`, public files, Vite/PostCSS config and the packages those configs import (e.g. postcss-preset-mantine or `test_config`).   |  `[]` |
| <a id="fe_app-test_srcs"></a>test_srcs |  Test sources; defaults to `*.test/spec.ts(x)` under `src/`, plus TypeScript in `src/test/`, `__tests__/` and `__fixtures__/` directories.   |  `None` |
| <a id="fe_app-test_deps"></a>test_deps |  Additional test compilation/runtime dependencies.   |  `[]` |
| <a id="fe_app-test_shard_count"></a>test_shard_count |  Bazel shards for the cacheable Vitest test target, in place of the count derived from test files (e.g. for a suite of slow test files); 0 disables sharding. Keep it at most the number of test files: Vitest fails a shard with no test files.   |  `None` |
| <a id="fe_app-test_tags"></a>test_tags |  Tags for the cacheable Vitest test target.   |  `[]` |
| <a id="fe_app-stories"></a>stories |  Story sources; defaults to `src/**/*.stories.ts(x)`.   |  `None` |
| <a id="fe_app-story_deps"></a>story_deps |  Additional story dependencies (e.g. @storybook/react).   |  `[]` |
| <a id="fe_app-visibility"></a>visibility |  Visibility of entry points, compiled src and stories.   |  `["//visibility:private"]` |


