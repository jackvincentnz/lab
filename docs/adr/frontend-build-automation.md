# Frontend BUILD automation

## Status

Proposed.

Date: 2026-10-07
Decision owner: Jack Vincent

## Context

[Gazelle](../gazelle.md) generates Java BUILD files, checked in CI. Frontend BUILD files are
written by hand. Each app is one package with one [`fe_app`](../../tools/bazel/fe.bzl) call, which
globs `src/` recursively and expands into separate production, test, and story `ts_project` targets.
People maintain its three dependency lists: `deps`, `test_deps`, and `story_deps`. Nothing reports
a dependency that is no longer imported.

[Aspect's JS/TS Gazelle extension](https://github.com/aspect-build/aspect-gazelle/tree/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js)
is the maintained option. It handles one package per app: with
`# gazelle:generation_mode update_only`, one `ts_project` in an existing BUILD file owns the
sources of subdirectories that have no BUILD file of their own. But it writes separate
`ts_project` and `js_library` targets, each with its own `deps`. It has no way to fill the three
lists of one macro call: `map_kind` renames a rule kind and keeps its attributes, so mapping
`ts_project` to `fe_app` would treat each source group as a whole app.

## Decision

Keep frontend dependencies hand-written in `fe_app`, and keep one package per app. Do not split
apps into directory packages, or `fe_app` into separate calls, only to fit the extension.

This accepts that unused frontend dependencies stay until someone removes them.

## Consequences

- The `fe_app` interface and the frontend BUILD files stay as they are, with no new tooling.
- Frontend deps can drift from imports, as Java deps did before Gazelle. A missing dependency
  fails the `tsc` check; an unused one goes unnoticed.

## Alternatives considered

- **Aspect's extension with `fe_app` replaced by separate `ts_project` calls.** Fits the
  extension's model, keeps one package per app, and needs no custom code. It changes the macro
  contract: the BUILD file would hold the compilation targets and the macro only the Vite and
  Vitest targets. Runtime inputs that no source imports, such as Vite and PostCSS config, stay
  hand-written, and imports of [generated GraphQL code](../../tools/bazel/graphql.bzl) need a
  `# gazelle:js_resolve` directive, as generated Java code does.
- **A Gazelle language that writes `fe_app`.** Keeps the macro, but the repository would own
  import parsing, resolution, and the macro's test and story patterns.
- **Aspect's extension in the existing `//tools/gazelle` binary.** `gazelle_binary` accepts several
  languages, so one binary and one `-mode=diff` CI check would cover Java and frontend. Building the
  extension from source needs rules_go 0.63.0 and Go 1.27.0, where this repository uses 0.59.0 and
  1.24.12, and a Rust and LLVM toolchain for its cgo import parser. None of this has been built
  here.
- **Aspect's prebuilt runner.** Needs no compilation, but its fixed language set has no Java, so
  Java would need the existing binary as well: two generators and two checks.

## Reconsider when

- Unused frontend dependencies slow builds or reviews: evaluate Aspect's extension with separate
  `ts_project` calls, in the existing Gazelle binary.
- The repository moves to rules_go 0.63.0 and Go 1.27.0 or later: the combined binary costs only
  the Rust and LLVM toolchain.

## References

- [Aspect JS/TS extension and its build setup](https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/README.md),
  at the evaluated commit.
- [Flat package fixture](https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/tests/gazelle_generation_mode/none/BUILD.out)
  and [source group fixture](https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/tests/groups_simple_files/BUILD.out).
- [Aspect prebuilt runner and its languages](https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/README.md#prebuilt).
- [Gazelle directives](https://github.com/bazel-contrib/bazel-gazelle/blob/v0.54.0/gazelle-reference.md#directives).
