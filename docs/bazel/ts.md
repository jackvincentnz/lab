<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# TypeScript

Shared TypeScript configuration and transpilation defaults.

<a id="node_ts_project"></a>

## node_ts_project

```starlark
load("//tools/bazel:ts.bzl", "node_ts_project")

node_ts_project(name, **kwargs)
```

Compile Node TypeScript using ts_project's shared defaults.

Defaults to //:tsconfig_node and SWC with //:.swcrc.node, and adds Node type
declarations to deps. Configuration and transpiler can be overridden.

**PARAMETERS**

| Name                                      | Description                      | Default Value |
| :---------------------------------------- | :------------------------------- | :------------ |
| <a id="node_ts_project-name"></a>name     | Target name.                     | none          |
| <a id="node_ts_project-kwargs"></a>kwargs | Attributes passed to ts_project. | none          |

<a id="ts_config"></a>

## ts_config

```starlark
load("//tools/bazel:ts.bzl", "ts_config")

ts_config(name, **kwargs)
```

Declare a [TypeScript configuration](https://github.com/aspect-build/rules_ts/blob/v3.10.1/ts/defs.bzl).

**PARAMETERS**

| Name                                | Description                                       | Default Value |
| :---------------------------------- | :------------------------------------------------ | :------------ |
| <a id="ts_config-name"></a>name     | Target name.                                      | none          |
| <a id="ts_config-kwargs"></a>kwargs | Attributes passed to the upstream ts_config rule. | none          |

<a id="ts_project"></a>

## ts_project

```starlark
load("//tools/bazel:ts.bzl", "ts_project")

ts_project(name, **kwargs)
```

Compile TypeScript with the repository's shared configuration and SWC.

Defaults tsconfig to //:tsconfig_base, enables declarations, declaration
maps, source maps and JSON module resolution, and uses SWC with //:.swcrc.
Each default can be overridden. Coverage builds include the original
sources in runtime data so Vitest can remap coverage.

**PARAMETERS**

| Name                                 | Description                                                                                           | Default Value |
| :----------------------------------- | :---------------------------------------------------------------------------------------------------- | :------------ |
| <a id="ts_project-name"></a>name     | Target name.                                                                                          | none          |
| <a id="ts_project-kwargs"></a>kwargs | Attributes passed to [ts_project](https://github.com/aspect-build/rules_ts/blob/v3.10.1/ts/defs.bzl). | none          |
