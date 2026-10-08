"""# Java

Shared Java wrappers keep external rules and test defaults in one place.
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
    """Declare a Java binary with [rules_java](https://github.com/bazelbuild/rules_java/blob/9.9.0/java/defs.bzl).

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _java_binary(
        name = name,
        **kwargs
    )

def java_library(name, **kwargs):
    """Declare a Java library with [rules_java](https://github.com/bazelbuild/rules_java/blob/9.9.0/java/defs.bzl).

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _java_library(
        name = name,
        **kwargs
    )

def java_test_suite(name, **kwargs):
    """Declare a [JUnit test suite](https://github.com/bazel-contrib/rules_jvm/blob/v0.34.0/README.md#java_test_suite).

    Infers test class names using the .nz. and .lab. package prefixes and applies
    the shared test defaults: small size, the test Spring profile unless set
    explicitly, and Logback added to runtime dependencies.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
    _java_test_suite(
        name = name,
        package_prefixes = [".nz.", ".lab."],
        **_test_defaults(kwargs)
    )

def java_junit5_test(name, **kwargs):
    """Declare a [JUnit 5 test](https://github.com/bazel-contrib/rules_jvm/blob/v0.34.0/README.md#java_junit5_test).

    Defaults to small size and the test Spring profile unless set explicitly,
    and adds Logback to runtime dependencies.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying rule.
    """
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
