"""# Vitest

Shared Vitest run and watch defaults.
"""

load("@npm//:vitest/package_json.bzl", vitest_bin = "bin")
load("//tools/bazel:js.bzl", "js_run_devserver")

def vitest_run(name, **kwargs):
    """Run cacheable Vitest tests in the calling package by default.

    Prepends run to args and defaults the test size to small.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    vitest_bin.vitest_test(
        name = name,
        testonly = True,
        args = ["run"] + kwargs.pop("args", []),
        size = kwargs.pop("size", "small"),
        chdir = kwargs.pop("chdir", native.package_name()),
        **kwargs
    )

def vitest_watch(name, **kwargs):
    """Run Vitest in watch mode in the calling package by default.

    Prepends watch to args; pass --ui in args to open the test UI.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    js_run_devserver(
        name = name,
        testonly = True,
        args = ["watch"] + kwargs.pop("args", []),
        tool = "//tools/bazel/vitest:vitest_binary",
        chdir = kwargs.pop("chdir", native.package_name()),
        **kwargs
    )
