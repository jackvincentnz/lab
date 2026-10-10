# Frontend BUILD automation

## Status

Proposed; no frontend generator or target-layout change is adopted.

Date: 2026-10-07

## Context

[Issue #46](https://github.com/jackvincentnz/lab/issues/46) asks whether Gazelle can maintain
dependencies for flattened frontends and share Java's binary and CI check.
[Java generation](java-build-files.md) is already accepted.

[`fe_app`](../../tools/bazel/fe.bzl) declares production, test/helper, and optional story
compilation in one BUILD call. It discovers sources recursively, puts production imports in
`deps`, and adds `test_deps` and `story_deps` to the respective compilations. Its `data` serves
Vite/Vitest runtime inputs. The inner `src_ts`, `test_ts`, and `stories` targets are created by
the macro; they are not separate calls that Gazelle can edit.

## Findings

### Flat packages are supported

Aspect's JS/TS extension does not require a BUILD file per source directory. With
`# gazelle:generation_mode update_only`, Gazelle 0.54.0's [walker][walker] collects recursive files within an
existing Bazel package. Nested BUILD files remain package boundaries. The upstream
[fixture input][flat-in] and [expected output][flat-out] demonstrate one `ts_project` owning
`a/lib-a.ts` and `a/a2/lib-a2.ts`, while depending on a separate existing `a/a1` package.
The repository's `src/__generated__` BUILD packages can therefore remain separate.

This directive limits BUILD-file creation, not creation of additional rules in an existing
BUILD file. Scope it to frontend packages; applying it globally would also change Java's
generation behavior. The older `js_generation_mode` directive is deprecated.

### The existing macro needs adaptation

The extension's [generation][generate] and [kind definitions][kinds] describe separate
`ts_project`/`js_library` source-group rules with a `deps` attribute. Its
[custom-group fixture][groups] emits separate production, test, and custom targets.
It has no `fe_app` kind or ownership of `test_deps` and `story_deps`.

Gazelle's [`map_kind`][gazelle-reference] changes a rule's kind and load, preserving the
attribute schema. It does not combine three generated rules into one macro call or rename
their dependency attributes. Mapping `ts_project` to `fe_app` would treat each source group
as a whole app and can emit unsupported attributes such as `testonly` or `tsconfig`.
Naming generated targets `src_ts` and `test_ts` instead would collide with the macro's
expanded targets. Thus flat source collection is supported, but maintaining the present
`fe_app` interface requires generator adaptation, not directives alone.

### One binary and check are possible, with integration work

Gazelle 0.54.0's [`gazelle_binary`][gazelle-binary] accepts multiple Go language libraries
exporting `NewLanguage`. Aspect's [JS library][js-build] and [constructor][js-language]
fit that interface alongside `contrib_rules_jvm`. A compatible source-built extension could
use the existing `//tools/gazelle` invocation and `-mode=diff` CI check.

This is source-level compatibility, not a tested combined build. The evaluated upstream
[module][js-module] and [Go module][js-go-module] require rules_go 0.63.0 and Go 1.27.0;
this repository selects rules_go 0.59.0 and Go 1.24.12. Its Rust parser links through cgo,
and upstream [documents][js-readme] LLVM/Rust toolchains and root build flags. These costs
need a separate reviewed integration proof on macOS and Linux.

Aspect's [prebuilt runner][aspect-readme] avoids compiling that parser, but its fixed
language set excludes the `contrib_rules_jvm` Java extension. It cannot simply replace
the repository's combined binary. Two binaries could share a CI step, at the cost of two
generation paths.

## Recommendation for review

Keep current frontend dependency maintenance until choosing an adaptation approach.
Do not split source directories merely to accommodate Gazelle.

| Option                                                  | Benefit                                                        | Tradeoff                                                                                |
| ------------------------------------------------------- | -------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| Keep manual `fe_app` deps.                              | Preserves the existing interface with no new tooling.          | Unused dependency cleanup remains manual.                                               |
| Adapt a generator to `fe_app`.                          | Can preserve one call and automate its three dependency lists. | Repository-specific generation, indexing, and source classification must be maintained. |
| Expose separate compilation calls for Aspect to manage. | Reuses its existing source-group model within flat packages.   | Changes the macro contract and BUILD ownership; requires an explicit layout decision.   |

An adaptation would need to mirror the macro's test/helper/story patterns and explicit
source overrides, account for dependencies inherited from production, and resolve hidden
outputs such as [`graphql_codegen_ts`](../../tools/bazel/graphql.bzl). Runtime configuration,
assets, implicit type dependencies, and workspace npm links also need ownership decisions;
import parsing alone does not establish all Vite/Vitest runtime inputs.

## Evidence and limits

Evaluated Aspect upstream commit
[`e60bf5b9`](https://github.com/aspect-build/aspect-gazelle/commit/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c)
and repository code. The fixtures above are upstream expected results, not locally executed
tests. No JS/TS extension, combined binary, or adapted `fe_app` generator was built here.
The recommendation leaves the implementation choice open.

[flat-in]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/tests/gazelle_generation_mode/none/BUILD.in
[flat-out]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/tests/gazelle_generation_mode/none/BUILD.out
[walker]: https://github.com/bazel-contrib/bazel-gazelle/blob/v0.54.0/v2/walk/walk.go
[generate]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/generate.go
[kinds]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/kinds.go
[groups]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/tests/groups_simple_files/BUILD.out
[gazelle-reference]: https://github.com/bazel-contrib/bazel-gazelle/blob/v0.54.0/gazelle-reference.md#directives
[gazelle-binary]: https://github.com/bazel-contrib/bazel-gazelle/blob/v0.54.0/internal/gazelle_binary.bzl
[js-build]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/BUILD.bazel
[js-language]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/language.go
[js-module]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/MODULE.bazel
[js-go-module]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/go.mod
[js-readme]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/language/js/README.md#build-setup
[aspect-readme]: https://github.com/aspect-build/aspect-gazelle/blob/e60bf5b9a1244d52bc962edd4dde90f3c48cb18c/README.md
