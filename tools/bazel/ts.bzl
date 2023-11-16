"""
This module contains typescript wrapper macros.
"""

load("@aspect_rules_ts//ts:defs.bzl", _ts_project = "ts_project")
load("@bazel_skylib//lib:partial.bzl", "partial")
load("@aspect_rules_swc//swc:defs.bzl", "swc")

def ts_project(name, **kwargs):
    """ts_project() macro with default tsconfig and aligning params.
    """

    _ts_project(
        name = name,

        # Default tsconfig and aligning attributes
        tsconfig = kwargs.pop("tsconfig", "//:tsconfig_base"),
        declaration = kwargs.pop("declaration", True),
        declaration_map = kwargs.pop("declaration_map", True),
        source_map = kwargs.pop("source_map", True),
        deps = kwargs.pop("deps", []),
        transpiler = kwargs.pop("transpiler", partial.make(
            swc,
            swcrc = "//:.swcrc",
        )),

        # Allow anything else to be overridden
        **kwargs
    )

def node_ts_project(name, **kwargs):
    ts_project(
        name = name,

        # Default tsconfig and aligning attributes
        tsconfig = kwargs.pop("tsconfig", "//:tsconfig_node"),
        declaration_map = kwargs.pop("declaration_map", False),

        # Allow anything else to be overridden
        **kwargs
    )
