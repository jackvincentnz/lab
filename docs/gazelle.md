# Gazelle

[Gazelle](https://github.com/bazel-contrib/bazel-gazelle) generates the `srcs`, `deps`, and
`exports` of Java BUILD targets from the source files and their imports. See the
[Java BUILD files decision](adr/java-build-files.md) for why, and the
[Bazel contribution guidelines](contributing/bazel.md#java-build-files) for what to write by hand.

## Running

Update BUILD files from the repository root:

```sh
aspect gazelle
```

`aspect` is on `PATH` through the [development environment](tools.md). Without it, run
`bazel run //tools/gazelle`.

Check without changing files:

```sh
bazel run //tools/gazelle -- -mode=diff
```

CI runs this check before building, and fails when it prints a diff.

## Configuration

- [`tools/gazelle/BUILD.bazel`](../tools/gazelle/BUILD.bazel) builds Gazelle with the Java
  extension from `contrib_rules_jvm`, and maps `@RequiresDocker` to the Docker test tags.
- Directives in the root [`BUILD.bazel`](../BUILD.bazel) map rule kinds to the
  `//tools/bazel:java.bzl` wrappers, and resolve imports Gazelle cannot find on its own: generated
  code, and Java packages that more than one Maven artifact provides.
- A `# gazelle:` directive in another BUILD file applies to that directory and below, such as
  `# gazelle:java_module_granularity module` for a target built from subdirectories.

## Errors

- **`Unable to find package for import in any dependency`.** No target or Maven artifact provides
  the package. Add the artifact to `maven.install` in `MODULE.bazel`, or, for generated code, add
  `# gazelle:resolve java <package> <label>` to the root `BUILD.bazel`.
- **`Append one of the following to BUILD.bazel`.** Several Maven artifacts provide the package.
  Add the suggested `# gazelle:resolve` line for the artifact you use.
- **`could not merge expression`.** An attribute Gazelle owns uses an expression, such as `glob()`
  or a `+` concatenation. Replace it with a plain list, or delete it and let Gazelle write it.
- **`the java extension encountered errors that will create invalid build files`.** Gazelle
  stopped because of an earlier error; fix that one first.
