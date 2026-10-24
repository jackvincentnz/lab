"""# Frontend applications

Declare Vite applications.
"""

load("//tools/bazel:js.bzl", "js_library")
load("//tools/bazel:ts.bzl", "ts_project")
load("//tools/bazel/vite:vite.bzl", "vite_build", "vite_dev_server")
load("//tools/bazel/vitest:vitest.bzl", "vitest_run", "vitest_watch")

_SRC_PATTERNS = [
    "src/**/*.ts",
    "src/**/*.tsx",
]

_TEST_PATTERNS = [
    "src/**/*.test.ts",
    "src/**/*.test.tsx",
    "src/**/*.spec.ts",
    "src/**/*.spec.tsx",
    "src/**/__tests__/**/*.ts",
    "src/**/__tests__/**/*.tsx",
    "src/**/__fixtures__/**/*.ts",
    "src/**/__fixtures__/**/*.tsx",
    "src/test/**/*.ts",
    "src/test/**/*.tsx",
]

_STORY_PATTERNS = [
    "src/**/*.stories.ts",
    "src/**/*.stories.tsx",
]

_TEST_FILE_SUFFIXES = (".test.ts", ".test.tsx", ".spec.ts", ".spec.tsx")

# Without isolation, one worker ran about 0.2 s per mops test file in CI, so 100 files take about
# 20 s: under the ADR's 30 s target with room for slower files, and a third of the 60 s timeout.
_TEST_FILES_PER_SHARD = 100

def _test_shard_count(test_srcs):
    test_files = len([src for src in test_srcs if src.endswith(_TEST_FILE_SUFFIXES)])
    if test_files <= _TEST_FILES_PER_SHARD:
        return None
    return (test_files + _TEST_FILES_PER_SHARD - 1) // _TEST_FILES_PER_SHARD

def fe_app(
        name,
        srcs = None,
        deps = [],
        assets = [],
        data = [],
        test_srcs = None,
        test_deps = [],
        test_tags = [],
        stories = None,
        story_deps = [],
        visibility = ["//visibility:private"]):
    """Declare one flattened Vite application per Bazel package.

    Emits the named Vite dev server, `src_ts`, `src`, `build`, `preview`, `test_ts`,
    `test_run`, `test_watch`, `test_ui` and, when present, `stories`.
    By default, discover TypeScript under `src/` in the calling package, separating
    production, tests/helpers and stories. Explicit source lists override these
    defaults (including `[]`); keep overrides disjoint. Keep app-specific Vite/Vitest
    configuration in the calling package; tests consume compiled JS. `test_run`
    gets one Bazel shard for every 100 test files once it has more than 100.
    Stories compile separately for an app-owned Storybook configuration.

    Args:
        name: Target name for the Vite dev server (normally the package basename).
        srcs: Production sources; defaults to `src/**/*.ts(x)`, excluding the
            test/helper and story patterns below.
        deps: Libraries imported by production TypeScript (e.g. React or generated
            GraphQL code). Used for typechecking and included in Vite runfiles.
        assets: Assets imported by production sources.
        data: Files and packages consumed by Vite/Vitest at runtime, rather than
            by application TypeScript compilation: `index.html`, `package.json`,
            public files, Vite/PostCSS config and the packages those configs
            import (e.g. postcss-preset-mantine or `test_config`).
        test_srcs: Test sources; defaults to `*.test/spec.ts(x)` under `src/`, plus
            TypeScript in `src/test/`, `__tests__/` and `__fixtures__/` directories.
        test_deps: Additional test compilation/runtime dependencies.
        test_tags: Tags for the cacheable Vitest test target.
        stories: Story sources; defaults to `src/**/*.stories.ts(x)`.
        story_deps: Additional story dependencies (e.g. @storybook/react).
        visibility: Visibility of entry points, compiled src and stories.
    """
    if srcs == None:
        srcs = native.glob(
            _SRC_PATTERNS,
            exclude = _TEST_PATTERNS + _STORY_PATTERNS,
            allow_empty = True,
        )
    if test_srcs == None:
        test_srcs = native.glob(
            _TEST_PATTERNS,
            exclude = _STORY_PATTERNS,
            allow_empty = True,
        )
    if stories == None:
        stories = native.glob(_STORY_PATTERNS, allow_empty = True)

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
        shard_count = _test_shard_count(test_srcs),
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
