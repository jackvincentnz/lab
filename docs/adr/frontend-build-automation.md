# Frontend BUILD automation

## Status

Proposed.

Date: 2026-10-17
Decision owner: Jack Vincent

## Context

Each frontend app is one package with one [`fe_app`](../../tools/bazel/fe.bzl) call. It globs
`src/`, so files need no BUILD edits, but 65 dependency labels in frontend BUILD files are written
by hand. A missing dependency fails `tsc`; an unused one stays, as `clsx` did in Mops.

The target is the structure Aspect's JS/TS Gazelle extension, `rules_js`, and `rules_ts` are
designed for, even if `fe_app` changes, judged on two criteria:

1. **Maintainability.** Generation keeps BUILD files right as files and imports are added, removed,
   or moved, little hand-written Bazel remains, and the result is easy to follow.
2. **Build performance.** Clean, single-file, and shared-library rebuilds of the largest app, for
   local development and CI, including the overhead of finer-grained targets.

Generating deps and splitting targets are separate choices. A prototype tried one generated
`ts_project` for sources and one for tests per package, and Gazelle's default of one per directory.

## Decision

Generate the TypeScript targets of each frontend package with Aspect's prebuilt Gazelle runner, and
keep one package per app and per library:

- Gazelle owns the `ts_project` targets for `src/` and tests, their `srcs`, `deps`, and imported
  `assets`, and the `npm_link_all_packages` of each workspace package.
- `fe_app` takes those targets and declares only the Vite dev server, build, and preview, and the
  Vitest run, watch, and UI targets. Vite and PostCSS config, `index.html`, public files, and the
  packages that config imports stay a hand-written `data` list.
- GraphQL codegen, the OCI image, and Storybook stay hand-written. A `js_resolve` directive maps
  imports of generated GraphQL code to its target.
- JS/TS directives use the `# aspect:` prefix, which the Java generator ignores. The root disables
  the language, and each frontend package enables it.
- `//tools/gazelle` runs the Java and JS generators in turn, and `//tools/gazelle:check`, run in CI,
  fails when either would change a file.

Do not split frontend packages per directory: it cost hand-written lists and gave no measured gain.
This accepts a second generator, a prebuilt binary downloaded per platform, and four
`unknown directive` warnings on stderr when the JS runner reads Java directives.

In the prototype, each change below was followed by a Gazelle run and no BUILD edits, and the build
and tests passed:

- **Add.** A new source and test file were added to the source and test targets.
- **Move.** A file moved between directories was renamed in `srcs`.
- **Delete.** A deleted directory removed its sources, test, and CSS asset.
- **Import another package.** Swapping `remark-gfm` for `clsx` added and removed both deps.
- **Unknown package.** An import that the lockfile does not contain failed Gazelle with its name.

Measured on an Apple M3 Pro, median of 3 runs (5 for the Bubbles bundle), `bazel shutdown` before
each clean run. "Ran" counts executed actions, which a remote cache would otherwise fetch; with a
warm cache the clean build ran none and fetched them all.

| Change                                   | Hand-written deps | Per package     | Per directory   |
| ---------------------------------------- | ----------------- | --------------- | --------------- |
| Clean, no cache: Mops bundle             | 25.9 s, ran 931   | 26.0 s, ran 931 | 27.2 s, ran 939 |
| Clean, no cache: Mops tests after that   | 9.1 s, ran 68     | 9.4 s, ran 68   | 9.5 s, ran 76   |
| Clean, disk cache: Mops bundle           | 6.2 s             | 6.1 s           | 6.3 s           |
| Mops component body: bundle              | 6.7 s, ran 3      | 6.8 s, ran 3    | 6.7 s, ran 3    |
| Mops component body: dev server          | 0.8 s, ran 2      | 0.7 s, ran 2    | 0.7 s, ran 2    |
| Mops component body: Vitest              | 8.9 s, ran 4      | 8.0 s, ran 4    | 8.0 s, ran 4    |
| Mops component body: type-check          | 1.8 s, ran 2      | 1.9 s, ran 2    | 1.6 s, ran 2    |
| Mops component export: type-check        | 3.0 s, ran 3      | 3.1 s, ran 3    | 6.1 s, ran 7    |
| Bubbles component: both org app bundles  | 2.8 s, ran 7      | 2.2 s, ran 7    | 2.0 s, ran 7    |
| Bubbles component: Bubbles and app tests | 8.3 s, ran 11     | 8.0 s, ran 11   | 8.6 s, ran 11   |

Splitting cannot help the bundle, dev server, or Vitest: they read SWC output, already one action
per file, and Vite and Vitest run once per app. `tsc` runs only for type-check outputs, as in
`bazel build //...`; per directory it re-checks less for a body change but more for an export
change. Apps import Bubbles through its npm package, copied whole, so splitting Bubbles does not
shrink its consumers' rebuilds. Mops does not use Bubbles. CI was not measured.

## Consequences

- New imports add deps and deleted imports remove them, without editing BUILD files, and CI fails on
  a stale BUILD file. The hand-written dependency labels drop from 65 to 3, the codegen config.
- `fe_app` shrinks from 154 to 66 lines. Each app gains about 8 directive lines, and generated
  `srcs` lists grow by a line per file, to 261 generated lines. An app's code is now `:src`, and its
  tests `:tests`.
- Bubbles needs its own `vite-env.d.ts` for CSS module types, instead of a shared library.
- Runtime-only inputs, such as Vite plugins and Vitest setup packages, are not imports. They stay in
  `data`, and Gazelle does not report one that is missing or unused.

## Alternatives considered

- **Keep hand-written deps in `fe_app`.** No new tooling, but deps drift from imports, as `clsx`
  did, and the build and test times above show no cost to generating them.
- **One generated package per directory.** Gazelle's default. Per app it needs hand-written lists of
  every test target for Vitest, of every story target for Storybook, and of GraphQL documents, which
  can no longer be globbed across packages, and a `vite-env.d.ts` in each directory that imports CSS
  modules. Mops went from 2 `ts_project` targets in 1 package to 19 in 10, for the results above.
- **Build the JS extension from source into the existing Gazelle binary.** One generator and one
  check, but it needs Go 1.27 and rules_go 0.63 or later, where this repository uses 1.24 and 0.59,
  a Rust toolchain, a direct `llvm` module at the version the extension pins, and four `.bazelrc`
  settings: two `BAZEL_*CPP_TOOLCHAIN` repo env values, `--linkopt=-no-pie`, and
  `--@llvm//config:experimental_stub_libgcc_s`. Go 1.27 may remove only `-no-pie`.
- **A Gazelle language that writes `fe_app`.** Keeps the macro, but the repository would own import
  parsing and resolution that the extension already provides.

## Reconsider when

- The `tsc` action for one app's sources takes more than 10 seconds, where Mops takes under 2:
  measure per-directory packages again on that app.
- The repository moves to Go 1.27 and rules_go 0.63 or later: evaluate building the extension into
  `//tools/gazelle`, with the `llvm` module and the four settings above, to remove the second
  generator.
- Aspect's runner honours `-mode=diff` from the command line: drop the separate check target.

## References

- [Prototype branch](https://github.com/jackvincentnz/lab/tree/spike/frontend-gazelle-js), with the
  [per-directory variant](https://github.com/jackvincentnz/lab/commit/c4111ef04fc54a65e1a9cb346f18b097c984d80e)
  in its history.
- [Aspect prebuilt runner](https://github.com/aspect-build/aspect-gazelle/blob/c1878a5332609645aa429bc881117be548f10285/prebuilt/README.md)
  and [JS/TS extension directives and build setup](https://github.com/aspect-build/aspect-gazelle/blob/c1878a5332609645aa429bc881117be548f10285/language/js/README.md),
  at the evaluated commit.
- [Gazelle directives](https://github.com/bazel-contrib/bazel-gazelle/blob/v0.54.0/gazelle-reference.md#directives).
