"""
This module contains common vite macros.
"""

load("//tools/bazel:js.bzl", "js_run_binary", "js_run_devserver")

def vite_build(name, **kwargs):
    js_run_binary(
        name = name,
        srcs = kwargs.pop("srcs", []),
        chdir = kwargs.pop("chdir", native.package_name()),
        args = ["build"] + kwargs.pop("args", []),
        tool = "//tools/bazel/vite:vite_binary",
        mnemonic = kwargs.pop("mnemonic", "ViteBuild"),
        out_dirs = kwargs.pop("out_dirs", ["dist"]),
        **kwargs
    )

def vite_dev_server(name, **kwargs):
    js_run_devserver(
        name = name,
        chdir = kwargs.pop("chdir", native.package_name()),
        data = kwargs.pop("data", []),
        args = ["--open", "--strictPort", "--host"],
        tool = "//tools/bazel/vite:vite_binary",
        **kwargs
    )
