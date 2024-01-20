"""
This module contains common vitest macros.
"""

load("@npm//:vitest/package_json.bzl", vitest_bin = "bin")

def vitest(name, **kwargs):
    vitest_bin.vitest_test(
        name = "test",
        size = kwargs.pop("size", "small"),
        **kwargs
    )
