"""
This module contains common react macros.
"""

load("//tools/bazel:ts.bzl", _ts_project = "ts_project")
load("//tools/bazel:js.bzl", "js_library")
load("@aspect_rules_jest//jest:defs.bzl", "jest_test")
load("@bazel_skylib//lib:partial.bzl", "partial")
load("@aspect_rules_swc//swc:defs.bzl", "swc")

# Common dependencies of react packages
COMMON_REACT_DEPS = [
    "//:node_modules/@types/react",
    "//:node_modules/@types/react-dom",
    "//:node_modules/react",
    "//:node_modules/react-dom",
]

# Common dependencies of react package tests
COMMON_REACT_TEST_DEPS = [
    "//:node_modules/@testing-library/jest-dom",
    "//:node_modules/@testing-library/react",
    "//:node_modules/@types/jest",
]

# Common dependencies of the jest runner
COMMON_JEST_DEPS = [
    "//:node_modules/jest-environment-jsdom",
    "//:node_modules/jest-transform-stub",
    "//:node_modules/@swc/jest",
]

ASSET_PATTERNS = [
    "*.svg",
    "*.css",
]

SRCS_PATTERNS = [
    "*.tsx",
    "*.ts",
]

# Filename conventions described at
# https://create-react-app.dev/docs/running-tests#filename-conventions
TEST_SRCS_PATTERNS = [
    "*.test.tsx",
    "*.test.ts",
    "*.spec.tsx",
    "*.spec.ts",
    "__tests__/*.test.tsx",
    "__tests__/*.test.ts",
    "__tests__/*.spec.tsx",
    "__tests__/*.spec.ts",
]

def react_lib(name, deps = [], test_deps = [], visibility = ["//visibility:public"]):
    """
    Bazel macro for compiling a React library. Creates {name}, test targets.

    Package structure:
      *.{ts,tsx}
      *.{css,svg}

    Tests:
      *.{spec,test}.{ts,tsx}
      __tests__/*.{spec,test}.{ts,tsx}

    Args:
      name: the rule name
      deps: deps required to build the lib
      test_deps: deps required to build the test lib
      visibility: visibility of the react lib
    """

    react_ts_project(
        name = "_ts",
        srcs = native.glob(
            include = SRCS_PATTERNS,
            exclude = TEST_SRCS_PATTERNS,
        ),
        deps = deps + COMMON_REACT_DEPS,
        visibility = ["//visibility:private"],
    )

    js_library(
        name = name,
        srcs = native.glob(ASSET_PATTERNS) + [":_ts"],
        visibility = visibility,
    )

    test_srcs = native.glob(TEST_SRCS_PATTERNS)

    if len(test_srcs) > 0:
        _unit_tests(
            name = "test",
            srcs = test_srcs,
            deps = [":_ts"] + test_deps,
            data = [":%s" % name],
        )

def _unit_tests(name, srcs, deps, data):
    react_ts_project(
        name = "_test_ts",
        srcs = srcs,
        deps = deps + COMMON_REACT_TEST_DEPS,
        testonly = True,
        visibility = ["//visibility:private"],
    )

    jest_test(
        name = name,
        config = "//tools/bazel/jest:jest_config",
        data = data + [":_test_ts"] + COMMON_JEST_DEPS,
        node_modules = "//:node_modules",
        visibility = ["//visibility:private"],
    )

def react_ts_project(name, **kwargs):
    _ts_project(
        name = name,

        # Default tsconfig and aligning attributes
        tsconfig = kwargs.pop("tsconfig", "//:tsconfig_react"),
        transpiler = kwargs.pop("transpiler", partial.make(
            swc,
            swcrc = "//:.swcrc.react",
        )),

        # Allow anything else to be overridden
        **kwargs
    )
