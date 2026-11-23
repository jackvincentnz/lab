"""# Packaging

Shared archive and directory packaging defaults.
"""

load("@aspect_bazel_lib//lib:copy_to_directory.bzl", _copy_to_directory = "copy_to_directory")
load("@rules_pkg//:pkg.bzl", _pkg_tar = "pkg_tar")

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

def tar(name, **kwargs):
    """Package files with [pkg_tar](https://bazelbuild.github.io/rules_pkg/latest.html#pkg_tar).

    Defaults to a `tar.gz` archive; override `extension` for another format.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _pkg_tar(
        name = name,
        extension = kwargs.pop("extension", "tar.gz"),
        **kwargs
    )
