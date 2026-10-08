"""# Packaging

Shared archive packaging defaults.
"""

load("@rules_pkg//:pkg.bzl", _pkg_tar = "pkg_tar")

def tar(name, **kwargs):
    """Package files with [pkg_tar](https://bazelbuild.github.io/rules_pkg/latest.html#pkg_tar).

    Defaults to a tar.gz archive; override extension for another format.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _pkg_tar(
        name = name,
        extension = kwargs.pop("extension", "tar.gz"),
        **kwargs
    )
