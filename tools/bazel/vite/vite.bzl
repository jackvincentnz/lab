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

    js_run_devserver(
        name = "preview",
        chdir = kwargs.pop("chdir", native.package_name()),
        data = [":%s" % name],
        args = [
            "preview",
            "--strictPort",  # fail if port already in use
            "--host",  # vite should listen to requests from container networks
        ],
        tool = "//tools/bazel/vite:vite_binary",
        **kwargs
    )

def vite_dev_server(name, **kwargs):
    js_run_devserver(
        name = name,
        chdir = kwargs.pop("chdir", native.package_name()),
        data = kwargs.pop("data", []),
        args = [
            "--strictPort",  # fail if port already in use
            "--host",  # vite should listen to requests from container networks
        ],
        tool = "//tools/bazel/vite:vite_binary",
        **kwargs
    )
