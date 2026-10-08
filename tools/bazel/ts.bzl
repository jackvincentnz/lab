"""# TypeScript

Shared TypeScript configuration and transpilation defaults.
"""

load("@aspect_rules_swc//swc:defs.bzl", "swc")
load("@aspect_rules_ts//ts:defs.bzl", _ts_config = "ts_config", _ts_project = "ts_project")
load("@bazel_skylib//lib:partial.bzl", "partial")

def ts_project(name, **kwargs):
    """Compile TypeScript with the repository's shared configuration and SWC.

    Defaults tsconfig to //:tsconfig_base, enables declarations, declaration
    maps, source maps and JSON module resolution, and uses SWC with //:.swcrc.
    Each default can be overridden. Coverage builds include the original
    sources in runtime data so Vitest can remap coverage.

    Args:
        name: Target name.
        **kwargs: Attributes passed to
            [ts_project](https://github.com/aspect-build/rules_ts/blob/v3.10.1/ts/defs.bzl).
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
    """Compile Node TypeScript using ts_project's shared defaults.

    Defaults to //:tsconfig_node and SWC with //:.swcrc.node, and adds Node type
    declarations to deps. Configuration and transpiler can be overridden.

    Args:
        name: Target name.
        **kwargs: Attributes passed to ts_project.
    """
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
    """Declare a [TypeScript configuration](https://github.com/aspect-build/rules_ts/blob/v3.10.1/ts/defs.bzl).

    Args:
        name: Target name.
        **kwargs: Attributes passed to the upstream ts_config rule.
    """
    _ts_config(
        name = name,
        **kwargs
    )
