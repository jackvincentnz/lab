"""
This module contains common java macros to avoid direct dependencies on external rules.
"""

load("@contrib_rules_jvm//java:defs.bzl", "JUNIT5_DEPS", _java_test_suite = "java_test_suite")
load(
    "@rules_java//java:defs.bzl",
    _java_binary = "java_binary",
    _java_library = "java_library",
)

TEST_DEPS = [
    "//libs/test/src/test/java/lab/test:test",
    "@maven//:org_assertj_assertj_core",
    "@maven//:org_junit_jupiter_junit_jupiter_api",
    "@maven//:org_mockito_mockito_core",
    "@maven//:org_mockito_mockito_junit_jupiter",
]

SPRING_TEST_DEPS = [
    "@maven//:org_springframework_boot_spring_boot_test",
    "@maven//:org_springframework_spring_test",
    "@maven//:org_springframework_spring_beans",
]

DGS_TEST_DEPS = [
    "@maven//:com_netflix_graphql_dgs_graphql_dgs",
    "@maven//:com_netflix_graphql_dgs_graphql_dgs_spring_graphql_test",
]

DGS_TEST_RUNTIME_DEPS = [
    "@maven//:com_netflix_graphql_dgs_dgs_starter",
    "@maven//:com_netflix_graphql_dgs_dgs_starter_test",
]

TEST_RUNTIME_DEPS = JUNIT5_DEPS + [
    "@maven//:ch_qos_logback_logback_classic",
]

SPRING_TEST_RUNTIME_DEPS = [
    "@maven//:org_springframework_boot_spring_boot_starter_test",
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
        runtime_deps = TEST_RUNTIME_DEPS + kwargs.pop("runtime_deps", []),
        deps = TEST_DEPS + kwargs.pop("deps", []),
        package_prefixes = [".nz.", ".lab."],
        runner = "junit5",
        env = env,

        # Allow anything else to be overridden
        **kwargs
    )
