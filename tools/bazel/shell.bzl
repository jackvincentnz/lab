"""Shell rule wrappers for repository tooling."""

load("@rules_shell//shell:sh_test.bzl", _sh_test = "sh_test")

def sh_test(name, **kwargs):
    _sh_test(name = name, **kwargs)
