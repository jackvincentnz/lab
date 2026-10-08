# TypeScript performance

Use the [TypeScript performance guide](https://github.com/microsoft/TypeScript/wiki/Performance)
to investigate slow type checking, declaration generation, or editor feedback.
These practices concern compiler work, rather than application runtime speed.
Measure a slow path before changing types or configuration.

## Keep types easy to check

- Prefer interfaces with `extends` when composing object types: they detect
  conflicting properties and let the checker cache relationships more readily
  than intersections. Keep unions where they express real alternatives.
- Give complex conditional or mapped types a reusable name. Avoid distributing
  expensive operations over large unions when a shared base type captures what
  the consumer needs.
- Try named return types at exported boundaries when inference produces large
  declarations. Keep convenient local inference; annotations are not needed
  everywhere.

For example, Mops defines `LineItem` and `Column` as interfaces in
[`spend-table/types.ts`](../../projects/mops/app/src/pages/spend/components/spend-table/types.ts).
Its [`mappers.ts`](../../projects/mops/app/src/pages/spend/mappers.ts) converts
generated GraphQL query data to explicit `LineItem[]` and `Column[]` results.
This gives consumers a small, named contract instead of exposing an inferred
query-shaped result. Bubbles similarly names its date-range type
`MonthRangeValue` in
[`MonthRangePicker.tsx`](../../libs/bubbles/src/components/MonthRangePicker/MonthRangePicker.tsx).
These are patterns to reuse, not measured bottlenecks to rewrite.

## Understand the existing configuration

[`tsconfig.base.json`](../../tsconfig.base.json) supplies browser defaults;
[`tsconfig.node.json`](../../tsconfig.node.json) supplies Node defaults.
The root [`tsconfig.json`](../../tsconfig.json) supports editors, including
generated sources under `dist/bin` through `rootDirs`.

| Setting                                                                        | Purpose and tradeoff                                                                                                                                                                                                                                              |
| ------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `types: []` in the browser config; `types: ["node"]` in the Node config.       | Limits ambient globals. Imported modules still retain their types; the list does not restrict imports. See [`types`](https://www.typescriptlang.org/tsconfig/types.html).                                                                                         |
| `strict: true`.                                                                | Includes `strictFunctionTypes`, which permits faster variance checks as well as stricter checking.                                                                                                                                                                |
| `skipLibCheck: true`, also enforced for Bazel by [`.bazelrc`](../../.bazelrc). | Skips checking declaration files, but still checks their use in source. This can hide conflicting dependency declarations; fix dependency conflicts at their source. See [`skipLibCheck`](https://www.typescriptlang.org/tsconfig/skipLibCheck.html).             |
| `isolatedModules: true`.                                                       | Checks compatibility with single-file transpilers. It does not itself speed up TypeScript emit. See [`isolatedModules`](https://www.typescriptlang.org/tsconfig/isolatedModules.html).                                                                            |
| `declaration: true` and `declarationMap: true`.                                | Supplies declarations and navigation information for consumers, so oversized inferred public types can add work.                                                                                                                                                  |
| Broad `include` globs; `exclude: ["node_modules", "dist"]`.                    | Covers repository sources and Storybook files for editors. Investigate unexpectedly included files before narrowing coverage. [`exclude`](https://www.typescriptlang.org/tsconfig/exclude.html) only filters discovery; imports can bring excluded files back in. |

## Respect build boundaries

The [`ts_project` wrapper](../../tools/bazel/ts.bzl) uses SWC to emit JavaScript
and TypeScript to check types and emit declarations. A working Vite page alone
does not establish that types pass; use the project's Bazel build and tests as
described in [Contributing](../../CONTRIBUTING.md#validating-a-change).

Bazel targets declare their own sources and dependencies. For example,
[`libs/bubbles/src/BUILD.bazel`](../../libs/bubbles/src/BUILD.bazel) separates
component sources from stories, and the
[`fe_app` macro](../../tools/bazel/fe.bzl) separates app sources, tests, and
stories. Keep those boundaries clear so unrelated sources do not grow the
same checking unit.

[TypeScript project references](https://www.typescriptlang.org/docs/handbook/project-references.html)
can split large editor projects, but this repository does not configure a
`references` graph. They require `composite` projects and managed declaration
outputs, and too many projects can repeat dependency checking.
[`incremental`](https://www.typescriptlang.org/tsconfig/incremental.html) saves
compiler state in `.tsbuildinfo`; that is distinct from Bazel's action cache.
Consider either only after measurement, with an explicit plan for how outputs
fit the Bazel build.

## Diagnose before tuning

From the repository root, after [tool setup](../tools.md), inspect the editor
configuration with the repository-pinned compiler:

```sh
pnpm exec tsc --project tsconfig.json --showConfig
pnpm exec tsc --project tsconfig.json --listFilesOnly
pnpm exec tsc --project tsconfig.json --noEmit --extendedDiagnostics
```

`--showConfig` reveals effective options; `--listFilesOnly` reveals the file set.
[`--extendedDiagnostics`](https://www.typescriptlang.org/tsconfig/extendedDiagnostics.html)
reports memory and time spent in compiler phases. Compare the same workload and
compiler version before and after a change. These commands inspect the root
editor project, not Bazel's per-target checking graph; generated files and
workspace dependencies must be available for meaningful results.

For a slow Bazel build, identify the affected target and distinguish compiler
time from dependency fetches, code generation, and bundling. For slow editor
feedback, check the selected TypeScript version and project, then investigate
plugins or a compiler hotspot using the upstream guide's
[diagnostics and tracing procedures](https://github.com/microsoft/TypeScript/wiki/Performance#investigating-issues).
Keep traces and logs out of this public repository: they can contain source,
paths, and other private information.
