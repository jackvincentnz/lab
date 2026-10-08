<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# DGS Java code generation

Generate Java types and clients from GraphQL schemas.

<a id="dgs_codegen"></a>

## dgs_codegen

```starlark
load("//tools/bazel/dgs:codegen.bzl", "dgs_codegen")

dgs_codegen(name, package_name, schemas)
```

Generate a Java source jar from GraphQL schemas with DGS client generation and BigDecimal mapped to java.math.BigDecimal.

**ATTRIBUTES**

| Name                                              | Description                                         | Type                                                                | Mandatory | Default |
| :------------------------------------------------ | :-------------------------------------------------- | :------------------------------------------------------------------ | :-------- | :------ |
| <a id="dgs_codegen-name"></a>name                 | A unique name for this target.                      | <a href="https://bazel.build/concepts/labels#target-names">Name</a> | required  |         |
| <a id="dgs_codegen-package_name"></a>package_name | Java package for the generated sources.             | String                                                              | required  |         |
| <a id="dgs_codegen-schemas"></a>schemas           | GraphQL schema files to generate Java sources from. | <a href="https://bazel.build/concepts/labels">List of labels</a>    | required  |         |

<a id="dgs_codegen_library"></a>

## dgs_codegen_library

```starlark
load("//tools/bazel/dgs:codegen.bzl", "dgs_codegen_library")

dgs_codegen_library(name, **kwargs)
```

Generate GraphQL Java sources and compile them into a Java library.

Creates name + "_srcs" using dgs_codegen, then a Java library named name.
The library includes the DGS shared core, Jackson annotations, and GraphQL
Java dependencies. Visibility defaults to private.

**PARAMETERS**

| Name                                          | Description                                                             | Default Value |
| :-------------------------------------------- | :---------------------------------------------------------------------- | :------------ |
| <a id="dgs_codegen_library-name"></a>name     | Target name.                                                            | none          |
| <a id="dgs_codegen_library-kwargs"></a>kwargs | Attributes passed to dgs_codegen, plus visibility for the Java library. | none          |
