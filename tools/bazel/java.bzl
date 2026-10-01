"""
This module contains common java macros to avoid direct dependencies on external rules.
"""

load("@contrib_rules_jvm//java:defs.bzl", _java_test_suite = "java_test_suite")
load(
    "@rules_java//java:defs.bzl",
    _java_binary = "java_binary",
    _java_library = "java_library",
)

# Only reached at runtime, so no test imports it.
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
    env = kwargs.pop("env", {})
    env.setdefault("SPRING_PROFILES_ACTIVE", "test")

    # Do not evaluate the default glob when callers supply explicit sources.
    srcs = kwargs.pop("srcs") if "srcs" in kwargs else native.glob(["*.java"])

    _java_test_suite(
        name = name,

        # Default attributes
        size = kwargs.pop("size", "small"),
        srcs = srcs,
        runtime_deps = _TEST_RUNTIME_DEPS + kwargs.pop("runtime_deps", []),
        package_prefixes = [".nz.", ".lab."],
        env = env,

        # Allow anything else to be overridden
        **kwargs
    )
