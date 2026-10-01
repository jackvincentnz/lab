"""
This module contains common java macros to avoid direct dependencies on external rules.
"""

load(
    "@contrib_rules_jvm//java:defs.bzl",
    _java_junit5_test = "java_junit5_test",
    _java_test_suite = "java_test_suite",
)
load(
    "@rules_java//java:defs.bzl",
    _java_binary = "java_binary",
    _java_library = "java_library",
)

# Gazelle writes compile deps and the JUnit runtime from imports; these are only
# reached at runtime, so it cannot infer them.
_TEST_RUNTIME_DEPS = [
    "@maven//:ch_qos_logback_logback_classic",
]

def java_binary(name, **kwargs):
    _java_binary(
        name = name,
        **kwargs
    )

def java_library(name, **kwargs):
    _java_library(
        name = name,
        **kwargs
    )

def java_test_suite(name, **kwargs):
    _java_test_suite(
        name = name,
        package_prefixes = [".nz.", ".lab."],
        **_test_defaults(kwargs)
    )

def java_junit5_test(name, **kwargs):
    _java_junit5_test(
        name = name,
        **_test_defaults(kwargs)
    )

def _test_defaults(kwargs):
    env = kwargs.pop("env", {})
    env.setdefault("SPRING_PROFILES_ACTIVE", "test")
    kwargs["env"] = env
    kwargs.setdefault("size", "small")
    kwargs["runtime_deps"] = _TEST_RUNTIME_DEPS + kwargs.pop("runtime_deps", [])
    return kwargs
