"""# Storybook

Shared Storybook build and development server defaults.
"""

load("//tools/bazel:js.bzl", "js_run_binary", "js_run_devserver")

def storybook_build(name, **kwargs):
    """Build Storybook in the calling package by default.

    Emits storybook-static and sets CACHE_DIR to .cache.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
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
    """Run Storybook on port 6006 in the calling package by default.

    Sets CACHE_DIR to .cache.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
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
