"""
This module contains common storybook macros.
"""

load("@aspect_rules_js//js:defs.bzl", "js_run_binary", "js_run_devserver")

def storybook_build(name, **kwargs):
    js_run_binary(
        name = name,
        srcs = kwargs.pop("srcs", []),
        chdir = kwargs.pop("chdir", native.package_name()),
        args = ["build"],
        tool = "//tools/bazel/storybook:storybook_binary",
        mnemonic = "StorybookBuild",
        out_dirs = ["storybook-static"],
        env = {
            "CACHE_DIR": ".cache",
        },
        **kwargs
    )

def storybook_dev_server(name, **kwargs):
    js_run_devserver(
        name = name,
        chdir = kwargs.pop("chdir", native.package_name()),
        args = ["dev", "-p", "6006"],
        data = kwargs.pop("data", []),
        tool = "//tools/bazel/storybook:storybook_binary",
        env = {
            "CACHE_DIR": ".cache",
        },
        **kwargs
    )
