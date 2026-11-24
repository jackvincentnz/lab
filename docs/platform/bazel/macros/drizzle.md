# Drizzle

Drizzle migration generation for a project's SQLite schema.

<!-- Generated with Stardoc: http://skydoc.bazel.build -->

<a id="drizzle_generate"></a>

## drizzle_generate

```starlark
load("//tools/bazel:drizzle.bzl", "drizzle_generate")

drizzle_generate(name, data, **kwargs)
```

Declare a target that generates a migration with `bazel run`.

Runs `drizzle-kit generate` in the calling package, which loads
`drizzle.config.ts` from there. `bazel run` starts in the runfiles, so the
config writes migrations under `BUILD_WORKSPACE_DIRECTORY`. Pass arguments
after `--`, such as `--name=add_due_date`.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="drizzle_generate-name"></a>name |  Target name.   |  none |
| <a id="drizzle_generate-data"></a>data |  The config and the compiled schema it names.   |  none |
| <a id="drizzle_generate-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


