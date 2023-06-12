"""
This module contains typescript wrapper macros.
"""

load("@aspect_rules_ts//ts:defs.bzl", _ts_project = "ts_project")

# Common dependencies of typescript projects
TS_DEPS = [
    "//:node_modules/tslib",  # TODO: can we make this a runtime dep somehow? E.g. all consumers of a compiled ts project have this included?
]

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
        supports_workers = kwargs.pop("source_map", False),  # workers are buggy, enable once confident they work as expected
        deps = kwargs.pop("deps", []) + TS_DEPS,

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
