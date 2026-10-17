# TypeScript contribution guidelines

TypeScript performance means how long the compiler takes to check types. Slow
checking slows Bazel builds and editor feedback, and has nothing to do with how
fast the app runs. The upstream
[TypeScript performance guide](https://github.com/microsoft/TypeScript/wiki/Performance)
explains the reasoning behind each guideline here.

## Writing types

- Describe object shapes with `interface` and combine them with `extends`
  rather than `&`, so that the compiler caches the result and reports
  conflicting properties.
- Give a complex type a name and reuse it, rather than repeating it inline, so
  that the compiler checks it once.
- Write the return type of exported functions, and leave local variables
  inferred, so that the compiler does not infer and emit a large type for every
  caller.
- Prefer one base type over a union of many similar types, so that the compiler
  compares fewer members.
- Map generated GraphQL results onto small named types, as the Mops spend table
  does in
  [`types.ts`](../../projects/mops/app/src/pages/spend/components/spend-table/types.ts)
  and [`mappers.ts`](../../projects/mops/app/src/pages/spend/mappers.ts), so
  that components depend on a contract you control rather than the generated
  query shape.

## Configuration

- Keep shared compiler settings in [`tsconfig.base.json`](../../tsconfig.base.json)
  and [`tsconfig.node.json`](../../tsconfig.node.json), and leave the root
  [`tsconfig.json`](../../tsconfig.json) to editors, so that every target checks
  with the same rules.
- Leave `skipLibCheck` on, as [`.bazelrc`](../../.bazelrc) sets it for the
  [`ts_project` wrapper](../../tools/bazel/ts.bzl), so that dependency
  declaration files are not re-checked on every build.
- Measure before changing a compiler setting, and say in the PR what you
  measured, so that settings change for a demonstrated reason.

## Finding a slow spot

- Run the compiler with diagnostics from the repository root, after
  [tool setup](../tools.md), so that you see time and memory per phase. A high
  check time points at expensive types.

  ```sh
  pnpm exec tsc --project tsconfig.json --noEmit --extendedDiagnostics
  ```

- Add `--listFilesOnly` to the same command, so that you can see whether
  unexpected files are being checked.
- Compare before and after a change with the same compiler version, so that the
  numbers mean something.
- Follow the upstream guide's
  [investigation steps](https://github.com/microsoft/TypeScript/wiki/Performance#investigating-issues)
  for deeper tracing, and keep traces out of this public repository, so that
  source paths stay private.
