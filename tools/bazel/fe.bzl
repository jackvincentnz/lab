"""
This module contains common front end macros.
"""

load("//tools/bazel:js.bzl", "js_library")
load("//tools/bazel:ts.bzl", "ts_project")

def fe_library(name, deps = [], visibility = ["//visibility:public"]):
    """
    Bazel macro for compiling a standard fe library. Creates {name}, gql, assets targets.

    Package structure:
      *.{ts,tsx}
      *.{css,svg}
      *.{gql}

    Args:
      name: the rule name
      deps: deps required to build the lib
      visibility: visibility of the lib
      """

    COMMON_REACT_DEPS = [
        "//:node_modules/@types/react",
        "//:node_modules/@types/react-dom",
        "//:node_modules/react",
        "//:node_modules/react-dom",
    ]

    SRC_PATTERNS = [
        "*.tsx",
        "*.ts",
    ]

    ASSET_PATTERNS = [
        "*.css",
        "*.svg",
    ]

    js_library(
        name = "gql",
        srcs = native.glob(["*.gql"]),
        visibility = visibility,
    )

    js_library(
        name = "assets",
        srcs = native.glob(ASSET_PATTERNS),
        visibility = visibility,
    )

    ts_project(
        name = "_ts",
        srcs = native.glob(SRC_PATTERNS),
        visibility = ["//visibility:private"],
        deps = COMMON_REACT_DEPS + deps,
    )

    js_library(
        name = name,
        srcs = [
            ":assets",
            ":_ts",
        ],
        visibility = visibility,
    )
