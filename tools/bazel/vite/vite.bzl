"""# Vite

Shared Vite build and development server defaults.
"""

load("//tools/bazel:js.bzl", "js_run_binary", "js_run_devserver")

def vite_build(name, **kwargs):
    """Build a Vite application and declare a preview server named preview.

    Runs in the calling package by default, emits the dist directory, and
    prepends build to args. The preview server uses strict port allocation
    and listens on all interfaces.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
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
    """Run Vite in the calling package by default.

    Uses strict port allocation and listens on all interfaces.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
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
