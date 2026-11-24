---
sidebar_position: 1
---

# Bazel

Bazel builds, tests, and runs everything in this repository. Run it from the
repository root. See the [Bazel contribution guidelines](../../contributing/bazel.md)
for BUILD file conventions, and [Gazelle](../tools/gazelle.md) for the tool that
generates Java BUILD file dependencies.

## Outputs

`.bazelrc` sets `--symlink_prefix=dist/`, so the convenience symlinks are
`dist/bin` and `dist/testlogs` rather than `bazel-bin` and `bazel-testlogs`.

Each checkout, including each worktree, has its own output base. Find it with
`bazel info output_base`. Setting `--output_base` or `--output_user_root` uses
a separate output base with a cold cache, and changing other startup options,
such as `--batch`, restarts the server. See [disk usage](disk-usage.md) for
where Bazel keeps its state and how to reclaim space.

## Dependency sources

External repositories live under `$(bazel info output_base)/external/`. Maven
sources jars are in `rules_jvm_external++maven+<group_artifact>_sources_<version>`
repositories.

## Sandbox network

`tools/preset.bazelrc` sets `--nosandbox_default_allow_network`, so actions and
tests have no network access unless their target is tagged `requires-network`.
A test that binds or connects to a socket without the tag fails with errors such
as `Operation not permitted`. See [testing](../../contributing/testing.md#bazel-test-targets)
for how test targets are tagged.

## Macro reference

[Stardoc](https://github.com/bazelbuild/stardoc) generates a page from the
docstrings of each module in `tools/bazel/`:

- [DGS Java code generation](macros/dgs.md).
- [Documentation generation](macros/docs.md).
- [Files](macros/files.md).
- [Frontend applications](macros/fe.md).
- [GraphQL clients](macros/graphql.md).
- [Java](macros/java.md).
- [JavaScript](macros/js.md).
- [OCI images and delivery](macros/oci.md).
- [Packaging](macros/pkg.md).
- [Storybook](macros/storybook.md).
- [TypeScript](macros/ts.md).
- [Vite](macros/vite.md).
- [Vitest](macros/vitest.md).

Edit the docstrings rather than the pages, then regenerate the pages so that
their diff tests in `bazel test //...` pass:

```sh
bazel run //docs/platform/bazel/macros:update
```

To document another module, add a `bzl_library` beside it, and a `stardoc`
target and `update` entry in `docs/platform/bazel/macros/BUILD.bazel`, then link
its page here.
