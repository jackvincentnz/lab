# TypeScript contribution guidelines

TypeScript performance means how long the compiler takes to check types, after
it has parsed the files. Slow checking slows Bazel builds and editor feedback,
and has nothing to do with how fast the app runs. The upstream
[TypeScript performance guide](https://github.com/microsoft/TypeScript/wiki/Performance)
explains the reasoning behind each guideline here.

## Writing types

- Describe object shapes with `interface` and combine them with `extends`
  rather than `&`, so that the compiler caches the result and reports
  conflicting properties.
- Give a conditional or generic type a name rather than writing it inline in a
  signature, so that the compiler can cache its result instead of working it
  out on every use.
- Write the return type of an exported function when the inferred type would be
  large, so that the declaration file the compiler writes for other targets
  carries a short name instead of the whole shape. Local variables and simple
  components can stay inferred.
- Prefer a shared base type over a union of more than about a dozen similar
  object types, so that checks do not slow down with the square of the union
  size.
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
- Leave `skipLibCheck` on in `tsconfig.base.json`, which
  [`.bazelrc`](../../.bazelrc) mirrors for every compiler run, so that the
  declaration files of dependencies are not type-checked on every build. The
  [`ts_project` wrapper](../../tools/bazel/ts.bzl) transpiles with SWC and
  type-checks with `tsc`.
- Measure before changing a compiler setting, and say in the PR what you
  measured, so that settings change for a demonstrated reason.

## Finding a slow spot

- Run `pnpm install` and build the target once with Bazel, so that the
  compiler can resolve the generated sources under `dist/bin` and the numbers
  reflect a complete check.
- Check `pnpm exec tsc --version` against the version in
  [`package.json`](../../package.json), so that you measure the compiler Bazel
  uses.
- Run the compiler with diagnostics from the repository root, so that you see
  time and memory per phase. A high check time points at expensive types.

  ```sh
  pnpm exec tsc --project tsconfig.json --noEmit --extendedDiagnostics
  ```

- Compare before and after a change with the same compiler version, so that the
  numbers mean something.
- Follow the upstream guide's
  [investigation steps](https://github.com/microsoft/TypeScript/wiki/Performance#investigating-issues)
  for deeper tracing, and keep traces out of this public repository, so that
  local machine paths stay private.
