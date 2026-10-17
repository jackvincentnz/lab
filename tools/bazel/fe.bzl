"""
Vite application targets around the TypeScript targets that Gazelle generates.
"""

load("//tools/bazel/vite:vite.bzl", "vite_build", "vite_dev_server")
load("//tools/bazel/vitest:vitest.bzl", "vitest_run", "vitest_watch")

def fe_app(
        name,
        srcs,
        tests = [],
        data = [],
        test_tags = [],
        visibility = ["//visibility:private"]):
    """Declare the Vite dev server, production build, and Vitest targets of one application.

    Gazelle generates the ts_project targets that compile the application and its tests, with
    their deps and imported assets. This macro only runs them.

    Emits the named Vite dev server, build, preview, test_run, test_watch and test_ui.

    Args:
        name: Target name for the Vite dev server (normally the package basename).
        srcs: The compiled application: the ts_project targets Gazelle generates for src/.
        tests: The compiled tests and test helpers.
        data: Files and packages that Vite or Vitest load at runtime rather than application
            TypeScript: index.html, package.json, public files, Vite and PostCSS config, and the
            packages those configs import.
        test_tags: Tags for the cacheable Vitest test target.
        visibility: Visibility of the entry points.
    """
    runtime = srcs + data

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

    test_runtime = runtime + tests

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
