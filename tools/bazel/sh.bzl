"""
This module contains common shell macros to avoid direct dependencies on external rules.
"""

load("@rules_shell//shell:sh_test.bzl", _sh_test = "sh_test")

def sh_test(name, **kwargs):
    _sh_test(
        name = name,
        **kwargs
    )
