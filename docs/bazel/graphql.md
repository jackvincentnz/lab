<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# GraphQL clients

Shared GraphQL client-preset code generation for frontend apps.

<a id="graphql_codegen"></a>

## graphql_codegen

```starlark
load("//tools/bazel:graphql.bzl", "graphql_codegen")

graphql_codegen(name, srcs, visibility, chdir)
```

Generate and compile GraphQL client-preset files in the calling package.

Creates the generation target `name` and a TypeScript target `name + "_ts"`.
By default, the calling BUILD file is expected in the app's src/**generated**
directory, and codegen.ts is loaded from the app root two levels above it.
Override chdir for other layouts. The config's output directory must match
the calling Bazel package, where the four client-preset outputs are declared.

**PARAMETERS**

| Name                                              | Description                                                                                                                                                                                 | Default Value              |
| :------------------------------------------------ | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :------------------------- |
| <a id="graphql_codegen-name"></a>name             | Name of the code generation target.                                                                                                                                                         | none                       |
| <a id="graphql_codegen-srcs"></a>srcs             | App-specific codegen config, GraphQL schema, and document targets.                                                                                                                          | none                       |
| <a id="graphql_codegen-visibility"></a>visibility | Visibility of the compiled TypeScript target.                                                                                                                                               | `["//visibility:private"]` |
| <a id="graphql_codegen-chdir"></a>chdir           | Working directory relative to the Bazel execroot, used to find codegen.ts and resolve its relative paths. Defaults to two levels above the calling package, not relative to this .bzl file. | `None`                     |
