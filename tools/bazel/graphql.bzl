"""Shared GraphQL client-preset code generation for frontend apps."""

load("@npm//:@graphql-codegen/cli/package_json.bzl", graphql_codegen_bin = "bin")
load("//tools/bazel:ts.bzl", "ts_project")

def graphql_codegen(name, srcs, visibility = ["//visibility:private"]):
    """Generate and compile a GraphQL client in an app's src/__generated__ package.

    Creates the generation target `name` and a TypeScript target `name + "_ts"`.
    Runs from the app root so codegen.ts can resolve its relative schema,
    document, and output paths.

    Args:
        name: Name of the code generation target.
        srcs: App-specific codegen config, GraphQL schema, and document targets.
        visibility: Visibility of the compiled TypeScript target.
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
        chdir = native.package_name() + "/../../",
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
