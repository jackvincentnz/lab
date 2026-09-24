"""
This module contains common ts macros to avoid direct dependencies on external rules.
"""

load("@aspect_rules_swc//swc:defs.bzl", "swc")
load("@aspect_rules_ts//ts:defs.bzl", _ts_config = "ts_config", _ts_project = "ts_project")
load("@bazel_skylib//lib:partial.bzl", "partial")

def ts_project(name, **kwargs):
    """ts_project() macro with default tsconfig and aligning params.
    """

    source_map = kwargs.pop("source_map", True)
    _ts_project(
        name = name,

        # Default tsconfig and aligning attributes
        tsconfig = kwargs.pop("tsconfig", "//:tsconfig_base"),
        declaration = kwargs.pop("declaration", True),
        declaration_map = kwargs.pop("declaration_map", True),
        source_map = source_map,
        resolve_json_module = kwargs.pop("resolve_json_module", True),
        deps = kwargs.pop("deps", []),
        # Vitest drops remapped coverage if the original source is absent.
        data = kwargs.pop("data", []) + select({
            "//tools/bazel:coverage_enabled": kwargs.get("srcs", []),
            "//conditions:default": [],
        }),
        transpiler = kwargs.pop("transpiler", partial.make(
            swc,
            swcrc = "//:.swcrc",
            source_maps = source_map,
        )),

        # Allow anything else to be overridden
        **kwargs
    )

def node_ts_project(name, **kwargs):
    ts_project(
        name = name,

        # Default tsconfig and aligning attributes
        tsconfig = kwargs.pop("tsconfig", "//:tsconfig_node"),
        transpiler = kwargs.pop("transpiler", partial.make(
            swc,
            swcrc = "//:.swcrc.node",
            source_maps = kwargs.get("source_map", True),
        )),
        deps = kwargs.pop("deps", []) + ["//:node_modules/@types/node"],

        # Allow anything else to be overridden
        **kwargs
    )

def ts_config(name, **kwargs):
    _ts_config(
        name = name,
        **kwargs
    )
