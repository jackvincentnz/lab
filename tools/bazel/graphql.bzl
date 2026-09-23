"""Shared GraphQL client-preset code generation for frontend apps."""

load("@npm//:@graphql-codegen/cli/package_json.bzl", graphql_codegen_bin = "bin")
load("//tools/bazel:ts.bzl", "ts_project")

def graphql_codegen(name, srcs, visibility = ["//visibility:private"], chdir = None):
    """Generate and compile GraphQL client-preset files in the calling package.

    Creates the generation target `name` and a TypeScript target `name + "_ts"`.
    By default, the calling BUILD file is expected in the app's src/__generated__
    directory, and codegen.ts is loaded from the app root two levels above it.
    Override chdir for other layouts. The config's output directory must match
    the calling Bazel package, where the four client-preset outputs are declared.

    Args:
        name: Name of the code generation target.
        srcs: App-specific codegen config, GraphQL schema, and document targets.
        visibility: Visibility of the compiled TypeScript target.
        chdir: Working directory relative to the Bazel execroot, used to find
            codegen.ts and resolve its relative paths. Defaults to two levels
            above the calling package, not relative to this .bzl file.
    """
    graphql_codegen_bin.graphql_codegen(
        name = name,
        srcs = ["//:node_modules/@graphql-codegen/client-preset"] + srcs,
        outs = [
            "fragment-masking.ts",
            "gql.ts",
            "graphql.ts",
            "index.ts",
        ],
        chdir = native.package_name() + "/../../" if chdir == None else chdir,
    )

    ts_project(
        name = name + "_ts",
        srcs = [":" + name],
        visibility = visibility,
        deps = [
            "//:node_modules/@graphql-typed-document-node/core",
            "//:node_modules/graphql",
        ],
    )
