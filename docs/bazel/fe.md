<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Frontend applications and libraries

Declare Vite applications and reusable frontend libraries.

<a id="fe_app"></a>

## fe_app

<pre>
load("//tools/bazel:fe.bzl", "fe_app")

fe_app(<a href="#fe_app-name">name</a>, <a href="#fe_app-srcs">srcs</a>, <a href="#fe_app-deps">deps</a>, <a href="#fe_app-assets">assets</a>, <a href="#fe_app-data">data</a>, <a href="#fe_app-test_srcs">test_srcs</a>, <a href="#fe_app-test_deps">test_deps</a>, <a href="#fe_app-test_tags">test_tags</a>, <a href="#fe_app-stories">stories</a>, <a href="#fe_app-story_deps">story_deps</a>,
       <a href="#fe_app-visibility">visibility</a>)
</pre>

Declare one flattened Vite application per Bazel package.

Emits the named Vite dev server, `src_ts`, `src`, `build`, `preview`, `test_ts`,
`test_run`, `test_watch`, `test_ui` and, when present, `stories`.
By default, discover TypeScript under `src/` in the calling package, separating
production, tests/helpers and stories. Explicit source lists override these
defaults (including `[]`); keep overrides disjoint. Keep app-specific Vite/Vitest
configuration in the calling package; tests consume compiled JS. Stories
compile separately for an app-owned Storybook configuration.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="fe_app-name"></a>name |  Target name for the Vite dev server (normally the package basename).   |  none |
| <a id="fe_app-srcs"></a>srcs |  Production sources; defaults to `src/**/*.ts(x)`, excluding the test/helper and story patterns below.   |  `None` |
| <a id="fe_app-deps"></a>deps |  Libraries imported by production TypeScript (e.g. React or generated GraphQL code). Used for typechecking and included in Vite runfiles.   |  `[]` |
| <a id="fe_app-assets"></a>assets |  Assets imported by production sources.   |  `[]` |
| <a id="fe_app-data"></a>data |  Files and packages consumed by Vite/Vitest at runtime, rather than by application TypeScript compilation: `index.html`, `package.json`, public files, Vite/PostCSS config and the packages those configs import (e.g. postcss-preset-mantine or `coverage_config`).   |  `[]` |
| <a id="fe_app-test_srcs"></a>test_srcs |  Test sources; defaults to `*.test/spec.ts(x)` under `src/`, plus TypeScript in `src/test/`, `__tests__/` and `__fixtures__/` directories.   |  `None` |
| <a id="fe_app-test_deps"></a>test_deps |  Additional test compilation/runtime dependencies.   |  `[]` |
| <a id="fe_app-test_tags"></a>test_tags |  Tags for the cacheable Vitest test target.   |  `[]` |
| <a id="fe_app-stories"></a>stories |  Story sources; defaults to `src/**/*.stories.ts(x)`.   |  `None` |
| <a id="fe_app-story_deps"></a>story_deps |  Additional story dependencies (e.g. @storybook/react).   |  `[]` |
| <a id="fe_app-visibility"></a>visibility |  Visibility of entry points, compiled src and stories.   |  `["//visibility:private"]` |


<a id="fe_library"></a>

## fe_library

<pre>
load("//tools/bazel:fe.bzl", "fe_library")

fe_library(<a href="#fe_library-name">name</a>, <a href="#fe_library-deps">deps</a>, <a href="#fe_library-test_deps">test_deps</a>, <a href="#fe_library-visibility">visibility</a>)
</pre>

Legacy directory-level frontend library; use fe_app for new applications.

Retained for existing directory-level consumers; migrate them separately.

### Requirements

- The package follows the prescribed [package structure](#package-structure).
- The package uses Vitest test syntax.

### Package structure

- `*.{ts,tsx}` - sources
- `*.{svg,css}` - assets
- `__tests__/*.test.{ts,tsx}` - tests
- `__tests__/*` - test sources
- `__fixtures__/*` - test fixtures

### Targets

See all expanded targets with `bazel query "//path/to/package/..."`

For example:
```shell
bazel query "//path/to/library/..."
```

#### Build targets

- `...` - Type check and transpile whole package including test sources.
- `:[target_name]` - The js_library which can be included in the deps of downstream rules.
- `:stories` - The stories which can be included in a storybook.

For example:

```shell
# e.g. Type check and transpile whole package including test sources.

bazel build //path/to/library/...
```

#### Test targets

- `:test` - runs tests in `./__tests__/` with vitest.

For example:

```shell
# e.g. Run tests as a single cacheable run.

bazel test //path/to/library:test
```

#### Run targets

- `:test_watch` - runs tests in watch mode.
- `:test_ui` - launches browser to run tests in ui mode.

For example:

```shell
# e.g. Run tests in watch mode.

ibazel run //path/to/library:test_watch
```


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="fe_library-name"></a>name |  Target name   |  none |
| <a id="fe_library-deps"></a>deps |  Deps required to build the lib   |  `[]` |
| <a id="fe_library-test_deps"></a>test_deps |  Deps required to build the test lib   |  `[]` |
| <a id="fe_library-visibility"></a>visibility |  visibility of the lib   |  `["//visibility:private"]` |


