"""
This module contains common packaging macros to avoid direct dependencies on external rules.
"""

load("@rules_pkg//:pkg.bzl", _pkg_tar = "pkg_tar")

def tar(name, **kwargs):
    _pkg_tar(
        name = name,
        extension = kwargs.pop("extension", "tar.gz"),
        **kwargs
    )
