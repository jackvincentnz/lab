"""
This module contains common vitest macros.
"""

load("//tools/bazel:js.bzl", "js_run_devserver")

def vitest_run(name, **kwargs):
    chdir = kwargs.pop("chdir", native.package_name())
    data = kwargs.pop("data", [])

    native.sh_test(
        name = name,
        testonly = True,
        srcs = ["//tools/bazel/vitest:coverage_runner.sh"],
        args = [
            "$(rootpath //tools/bazel/vitest:vitest_binary)",
            chdir if chdir != None else ".",
        ] + kwargs.pop("args", []),
        size = kwargs.pop("size", "small"),
        data = data + [
            "//tools/bazel/vitest:vitest_binary",
            "//:node_modules/@vitest/coverage-v8",
        ],
        **kwargs
    )

def vitest_watch(name, **kwargs):
    js_run_devserver(
        name = name,
        testonly = True,
        tool = "//tools/bazel/vitest:vitest_binary",
        chdir = kwargs.pop("chdir", native.package_name()),
        **kwargs
    )
