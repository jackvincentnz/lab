"""
This module contains common front end macros.
"""

load("//tools/bazel:js.bzl", "js_library", "js_run_devserver")
load("//tools/bazel:ts.bzl", "ts_project")
load("//tools/bazel/vite:vite.bzl", "vite_build", "vite_dev_server")
load("//tools/bazel/vitest:vitest.bzl", "vitest_run", "vitest_watch")

def fe_app(
        name,
        srcs,
        deps = [],
        assets = [],
        data = [],
        test_srcs = [],
        test_deps = [],
        test_tags = [],
        stories = [],
        story_deps = [],
        visibility = ["//visibility:private"]):
    """Declare one flattened Vite application per Bazel package.

    Emits the named Vite dev server, src_ts, src, build, preview, test_ts,
    test_run, test_watch, test_ui and, when supplied, stories.
    Pass disjoint production, test/helper and story sources. Keep app-specific
    Vite/Vitest configuration in the calling package; tests consume compiled JS.
    Stories compile separately for an app-owned Storybook configuration.

    Args:
        name: Target name for the Vite dev server (normally "app").
        srcs: Production TypeScript sources, excluding tests/helpers and stories.
        deps: Libraries imported by production TypeScript (e.g. React or generated
            GraphQL code). Used for typechecking and included in Vite runfiles.
        assets: Assets imported by production sources.
        data: Files and packages consumed by Vite/Vitest at runtime, rather than
            by application TypeScript compilation: index.html, package.json,
            public files, Vite/PostCSS config and the packages those configs
            import (e.g. postcss-preset-mantine or coverage_config).
        test_srcs: Test sources plus app-specific setup, helpers and fixtures.
        test_deps: Additional test compilation/runtime dependencies.
        test_tags: Tags for the cacheable Vitest test target.
        stories: Optional story sources, compiled separately from production/tests.
        story_deps: Additional story dependencies (e.g. @storybook/react).
        visibility: Visibility of entry points, compiled src and stories.
    """
    ts_project(
        name = "src_ts",
        srcs = srcs,
        visibility = ["//visibility:private"],
        deps = deps,
    )

    js_library(
        name = "src",
        srcs = assets,
        deps = [":src_ts"],
        visibility = visibility,
    )

    runtime = [":src"] + data

    vite_dev_server(
        name = name,
        data = runtime,
        visibility = visibility,
    )

    vite_build(
        name = "build",
        srcs = runtime,
        visibility = visibility,
    )

    ts_project(
        name = "test_ts",
        testonly = True,
        visibility = ["//visibility:private"],
        srcs = test_srcs,
        deps = [":src"] + deps + test_deps,
    )

    test_runtime = runtime + [":test_ts"]

    vitest_run(
        name = "test_run",
        data = test_runtime,
        tags = test_tags,
        visibility = visibility,
    )

    vitest_watch(
        name = "test_watch",
        data = test_runtime,
        visibility = visibility,
    )

    vitest_watch(
        name = "test_ui",
        args = ["--ui"],
        data = test_runtime + ["//:node_modules/@vitest/ui"],
        visibility = visibility,
    )

    if stories:
        ts_project(
            name = "stories",
            srcs = stories,
            deps = [":src"] + deps + story_deps,
            visibility = visibility,
        )

def fe_library(name, deps = [], test_deps = [], visibility = ["//visibility:private"]):
    """Legacy directory-level frontend library; use fe_app for new applications.

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

    Args:
      name: Target name

      deps: Deps required to build the lib

      test_deps: Deps required to build the test lib

      visibility: visibility of the lib
    """

    COMMON_REACT_DEPS = [
        "//:node_modules/@types/react",
        "//:node_modules/@types/react-dom",
        "//:node_modules/react",
        "//:node_modules/react-dom",
    ]

    SRC_PATTERNS = [
        "*.tsx",
        "*.ts",
    ]

    STORY_SRC_PATTERNS = [
        "*.stories.ts",
        "*.stories.tsx",
    ]

    ASSET_PATTERNS = [
        "*.css",
        "*.svg",
    ]

    js_library(
        name = "gql",
        srcs = native.glob(["*.gql"], allow_empty = True),
        visibility = visibility,
    )

    js_library(
        name = "assets",
        srcs = native.glob(ASSET_PATTERNS, allow_empty = True),
        visibility = visibility,
    )

    ts_project(
        name = "_ts",
        srcs = native.glob(
            include = SRC_PATTERNS,
            exclude = STORY_SRC_PATTERNS,
            # A package can contain TS, TSX, or just re-export dependencies.
            allow_empty = True,
        ),
        visibility = ["//visibility:private"],
        deps = COMMON_REACT_DEPS + deps,
    )

    js_library(
        name = name,
        srcs = [
            ":assets",
            ":_ts",
        ],
        visibility = visibility,
    )

    # Tests and stories are discovered only when present.
    tests = native.glob(
        ["__tests__/*.test.tsx", "__tests__/*.test.ts"],
        allow_empty = True,
    )
    if len(tests) > 0:
        _tests(
            name = "test",
            deps = test_deps + [":%s" % name],
        )

    stories = native.glob(STORY_SRC_PATTERNS, allow_empty = True)
    if len(stories) > 0:
        ts_project(
            name = "stories",
            srcs = stories,
            deps = [
                "//:node_modules/@storybook/react",
                ":%s" % name,
            ],
            visibility = ["//visibility:public"],
        )

def _tests(name, deps):
    ts_project(
        name = "_test_ts",
        srcs = native.glob(["__tests__/**/*"]) + native.glob(["__fixtures__/**/*"], allow_empty = True),
        deps = deps + [
            "//tools/bazel/vitest:utils",
        ],
    )

    vitest_run(
        name = "test",
        args = [
            "--config",
            "$(location //tools/bazel/vitest:vite_config)",
        ],
        data = [
            ":_test_ts",
            "//tools/bazel/vitest:vite_config",
            "//tools/bazel/vitest:package_json",
        ],
        chdir = None,
    )

    js_run_devserver(
        name = "test_ui",
        args = [
            "--ui",
            "--config",
            "$(location //tools/bazel/vitest:vite_config)",
        ],
        data = [
            "//tools/bazel/vitest:vite_config",
            "//tools/bazel/vitest:package_json",
            ":_test_ts",
            "//:node_modules/@vitest/ui",
        ],
        tool = "//tools/bazel/vitest:vitest_binary",
    )

    js_run_devserver(
        name = "test_watch",
        args = [
            "--config",
            "$(location //tools/bazel/vitest:vite_config)",
        ],
        data = [
            "//tools/bazel/vitest:vite_config",
            "//tools/bazel/vitest:package_json",
            ":_test_ts",
        ],
        tool = "//tools/bazel/vitest:vitest_binary",
    )
