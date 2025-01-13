"""
This module contains common vitest macros.
"""

load("@npm//:vitest/package_json.bzl", vitest_bin = "bin")
load("//tools/bazel:js.bzl", "js_run_devserver")

def vitest_run(name, **kwargs):
    vitest_bin.vitest_test(
        name = name,
        testonly = True,
        args = ["run"] + kwargs.pop("args", []),
        size = kwargs.pop("size", "small"),
        chdir = kwargs.pop("chdir", native.package_name()),
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
