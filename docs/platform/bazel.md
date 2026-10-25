---
sidebar_position: 1
---

# Bazel

Bazel builds, tests, and runs everything in this repository. Run it from the
repository root. See the [Bazel contribution guidelines](../contributing/bazel.md)
for BUILD file conventions, and [Gazelle](tools/gazelle.md) for the tool that
generates Java BUILD file dependencies.

## Outputs

`.bazelrc` sets `--symlink_prefix=dist/`, so the convenience symlinks are
`dist/bin` and `dist/testlogs` rather than `bazel-bin` and `bazel-testlogs`.

Each checkout, including each worktree, has its own output base. Find it with
`bazel info output_base`. Setting `--output_base` or `--output_user_root` uses
a separate output base with a cold cache, and changing other startup options,
such as `--batch`, restarts the server.

## Dependency sources

External repositories live under `$(bazel info output_base)/external/`. Maven
sources jars are in `rules_jvm_external++maven+<group_artifact>_sources_<version>`
repositories.

## Sandbox network

`tools/preset.bazelrc` sets `--nosandbox_default_allow_network`, so actions and
tests have no network access unless their target is tagged `requires-network`.
A test that binds or connects to a socket without the tag fails with errors such
as `Operation not permitted`. See [testing](../contributing/testing.md#bazel-test-targets)
for how test targets are tagged.

## Macro reference

[Stardoc](https://github.com/bazelbuild/stardoc) generates a page from the
docstrings of each module in `tools/bazel/`:

- [DGS Java code generation](../bazel/dgs.md).
- [Documentation generation](../bazel/docs.md).
- [Frontend applications](../bazel/fe.md).
- [GraphQL clients](../bazel/graphql.md).
- [Java](../bazel/java.md).
- [JavaScript](../bazel/js.md).
- [OCI images and delivery](../bazel/oci.md).
- [Packaging](../bazel/pkg.md).
- [Storybook](../bazel/storybook.md).
- [TypeScript](../bazel/ts.md).
- [Vite](../bazel/vite.md).
- [Vitest](../bazel/vitest.md).

Edit the docstrings rather than the pages, then regenerate the pages so that
their diff tests in `bazel test //...` pass:

```sh
bazel run //docs/bazel:update
```

To document another module, add a `bzl_library` beside it, and a `stardoc`
target and `update` entry in `docs/bazel/BUILD.bazel`, then link its page here.
