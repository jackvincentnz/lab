"""
This module contains common front end macros.
"""

load("//tools/bazel:js.bzl", "js_library", "js_run_devserver")
load("//tools/bazel:ts.bzl", "ts_project")
load("//tools/bazel/vitest:vitest.bzl", "vitest")

def fe_library(name, deps = [], test_deps = [], visibility = ["//visibility:private"]):
    """Bazel macro for packaging a standard front end library.

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
    bazel query "//apps/tasklist/src/tasks/..."
    ```

    #### Build targets

    - `...` - Type check and transpile whole package including test sources.
    - `:[target_name]` - The js_library which can be included in the deps of downstream rules.
    - `:stories` - The stories which can be included in a storybook.

    For example:

    ```shell
    # e.g. Type check and transpile whole package including test sources.

    bazel build //apps/tasklist/src/tasks/...
    ```

    #### Test targets

    - `:test` - runs tests in `./__tests__/` with vitest.

    For example:

    ```shell
    # e.g. Run tests as a single cacheable run.

    bazel test //apps/tasklist/src/tasks:test
    ```

    #### Run targets

    - `:test_watch` - runs tests in watch mode.
    - `:test_ui` - launches browser to run tests in ui mode.

    For example:

    ```shell
    # e.g. Run tests in watch mode.

    ibazel run //apps/tasklist/src/tasks:test_watch
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
        srcs = native.glob(["*.gql"]),
        visibility = visibility,
    )

    js_library(
        name = "assets",
        srcs = native.glob(ASSET_PATTERNS),
        visibility = visibility,
    )

    ts_project(
        name = "_ts",
        srcs = native.glob(
            include = SRC_PATTERNS,
            exclude = STORY_SRC_PATTERNS,
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

    tests = native.glob([
        "__tests__/*.test.tsx",
        "__tests__/*.test.ts",
    ])
    if len(tests) > 0:
        _tests(
            name = "test",
            deps = test_deps + [":%s" % name],
        )

    stories = native.glob(STORY_SRC_PATTERNS)
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
        srcs = native.glob(["__tests__/**/*", "__fixtures__/**/*"]),
        deps = deps + [
            "//tools/bazel/vitest:utils",
        ],
    )

    vitest(
        name = "test",
        args = [
            "run",
            "--config",
            "$(location //tools/bazel/vitest:vite_config)",
        ],
        data = [
            ":_test_ts",
            "//tools/bazel/vitest:vite_config",
            "//tools/bazel/vitest:package_json",
        ],
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
