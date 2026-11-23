"""# Files

Shared rules that copy and arrange files.
"""

load("@aspect_bazel_lib//lib:copy_to_directory.bzl", _copy_to_directory = "copy_to_directory")

def copy_to_directory(name, **kwargs):
    """Copy files into one output directory with [copy_to_directory](https://github.com/bazel-contrib/bazel-lib/blob/main/docs/copy_to_directory.md).

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _copy_to_directory(
        name = name,
        **kwargs
    )
