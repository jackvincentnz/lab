"""# Packaging

Shared archive packaging defaults.
"""

load("@rules_pkg//:pkg.bzl", _pkg_tar = "pkg_tar", _pkg_zip = "pkg_zip")
load("@rules_pkg//pkg:mappings.bzl", _pkg_files = "pkg_files")

def files(name, **kwargs):
    """Place files in an archive with [pkg_files](https://bazelbuild.github.io/rules_pkg/latest.html#pkg_files).

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _pkg_files(
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

def zip(name, **kwargs):
    """Package files with [pkg_zip](https://bazelbuild.github.io/rules_pkg/latest.html#pkg_zip).

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _pkg_zip(
        name = name,
        **kwargs
    )
