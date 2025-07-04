"""
This module contains common java macros.
"""

load("@contrib_rules_jvm//java:defs.bzl", "JUNIT5_DEPS", _java_test_suite = "java_test_suite")

TEST_DEPS = [
    "//libs/test/src/test/java/nz/geek/jack/test",
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

def java_test_suite(name, **kwargs):
    env = kwargs.pop("env", {})
    env.setdefault("SPRING_PROFILES_ACTIVE", "test")

    _java_test_suite(
        name = name,

        # Default attributes
        size = kwargs.pop("size", "small"),
        srcs = kwargs.pop("srcs", native.glob(["*.java"])),
        runtime_deps = TEST_RUNTIME_DEPS + kwargs.pop("runtime_deps", []),
        deps = TEST_DEPS + kwargs.pop("deps", []),
        package_prefixes = [".nz.", ".lab."],
        runner = "junit5",
        env = env,

        # Allow anything else to be overridden
        **kwargs
    )
