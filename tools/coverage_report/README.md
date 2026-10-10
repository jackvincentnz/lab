# Coverage reporting completeness

This read-only report compares tracked Java and TS/TSX sources with LCOV and
explicit exclusions. It shows which sources are measured alongside the hit/miss
counts for the executable lines actually reported. It does not set a coverage
threshold or change collection, CI, or Codecov policy.

Run it from the repository root after collecting coverage:

```sh
bazel coverage --combined_report=lcov //...
bazel run //tools/coverage_report:report -- dist/out/_coverage/_coverage_report.dat --run 'bazel coverage --combined_report=lcov //...'
```

The text report lists every source by repository-relative path. JSON contains the
same rows, exclusion provenance, counts, and comparison metadata:

```sh
mkdir -p dist/coverage-inventory
bazel run //tools/coverage_report:report -- dist/out/_coverage/_coverage_report.dat --run 'bazel coverage --combined_report=lcov //...' --json > dist/coverage-inventory/before.json
# Repeat the same collection after making a change.
bazel run //tools/coverage_report:report -- dist/out/_coverage/_coverage_report.dat --run 'bazel coverage --combined_report=lcov //...' --baseline dist/coverage-inventory/before.json
```

Use the same run label only for comparable collection contexts. Include targets,
test/instrumentation filters, relevant compiler/collector settings, and whether
Docker tests were available. For example, a `--config=codex-cloud` run or a
frontend-only run needs its own label. The label is supplied by the caller;
LCOV cannot recover the command, environment, or tests that produced it.

Snapshots compare counts and per-file status transitions, and list added and
removed sources. They also flag changed source contents. Comparison requires
the same schema, reporting inventory/exclusions, classifier version, and run
label. Changing reporting scope requires a new baseline. Collection settings
not encoded by exclusions must be reflected in the run label. Keep local
snapshots under ignored `dist/`; they contain no source text but can expose
private source names if used with another repository.

## Reading the categories

- **Measured**: LCOV contains real `DA:<line>,<hits>` records. Zero-hit lines
  still establish measurement. The summary counts unique source lines, taking
  the union of hit status when test targets report overlapping sources.
- **Omitted executable**: Source analysis identifies runtime code, but LCOV has
  no executable line records. The row says whether the source is absent or has
  an empty baseline. The number of omitted executable lines remains unknown.
- **Excluded**: A reporting inventory exclusion, Codecov upload filter, or all
  applicable frontend collectors explicitly exclude this source. Rows identify
  the exclusion kind and preserve any observed LCOV
  lines, but excluded sources do not enter the measured production line totals.
- **Non-executable**: Source analysis establishes no runtime statements, such
  as type-only TypeScript or empty/package-only Java. An `LF:0` record alone
  never establishes this category.
- **Unresolved**: Source analysis cannot establish executable status, and no
  real LCOV lines are available. These files remain visible beside omissions;
  they are not treated as measured or non-executable.

The report intentionally does not calculate an overall executable-file coverage
percentage: unresolved files and omitted line counts make that denominator
unknown. Compare the measured, omitted, unresolved, and excluded counts together.
Measured line hit/miss totals describe only the files with real line records.

## Inventory and exclusions

The candidate inventory is Git's tracked `.java`, `.ts`, and `.tsx` files,
including tracked working-tree edits and staged additions. New files must be
staged to appear. A tracked file missing from disk is an input error. Git-ignored
generated outputs, assets, other source languages, dependencies, and untracked
files are outside this inventory.

[`inventory.json`](inventory.json) describes the reporting scope: production
sources rather than Java tests, end-to-end test infrastructure, build/test tools,
or configuration. Its exclusions document the scope of this report; they do not
claim that the native collector always excludes these files or change what it
collects. Each excluded row identifies its rule and reference.

The report also reads top-level `ignore` glob strings from `codecov.yml`.
For example, the existing `learn/**` upload filter is an explicit exclusion even
when native LCOV measures those sources. These rows identify `upload` as the
exclusion kind and retain their native line counts. Unsupported YAML aliases or
dynamic rules require adapting the reader. The report does not interpret or
change Codecov status thresholds.

Frontend collector includes and exclusions are read statically from
[`coverage.ts`](../bazel/vitest/coverage.ts) and tracked `vite.config.ts` calls to
`bazelCoverage`. The report matches emitted `.js` patterns against original
TS/TSX paths. The reporting inventory scopes the shared legacy library collector
to `libs/**`, where `fe_library` uses it; applications own their Vite configurations.
This prevents its broad fallback include from masking an app's explicit exclusion.
These scopes describe intended ownership, not the runfiles graph of a particular
test target. If any applicable collector includes a file, another collector's
exclusion does not exclude it globally. Dynamic patterns require updating the
reader; it fails rather than silently interpreting them. No collector discovery
match is evidence of neither exclusion nor measurement: those sources are still
classified and displayed as missing when LCOV omits them.

## Limits of the evidence

TypeScript analysis uses the TypeScript parser bundled with the repository's
pinned Prettier version. It recognizes erased type declarations and distinguishes
them from runtime syntax, retaining runtime imports and JSX. It is not SWC/V8
source-map analysis and cannot predict exact executable line numbers.
Import/re-export stubs and empty runtime declarations may emit
JavaScript while contributing no instrumentable lines; the omitted category is
therefore a runtime-code candidate list for investigation. Type-only modules
can be identified independently of `LF:0`.

Java executable lines depend on compiled bytecode, compiler-generated methods,
and JaCoCo filters. The report recognizes runtime operations such as `return`,
`new`, and control flow outside comments and literals. It leaves declaration-only
classes, interfaces, records, enums, initializers without recognized operations,
Unicode escapes, and text blocks unresolved without LCOV evidence. An empty
class can have an executable implicit constructor, so it is never declared
non-executable merely because its source body is empty. This source analysis
does not estimate omitted Java line counts.

LCOV paths must be repository-relative or absolute beneath the current
repository root. Absolute sandbox paths outside it are input errors; the tool
does not guess mappings or substitute generated JS for TS sources. LCOV sources
outside the candidate inventory appear separately, exposing stale paths or
generated-source mappings. Conflicting line totals, incomplete records, and line
numbers beyond the current source length are errors. Source changes that retain
valid line numbers cannot be detected from LCOV alone: collect coverage against
the checkout being inventoried. LCOV cannot prove whether an empty record came
from a compiler baseline, an unimported module, or a remapping failure.

The CLI returns success for valid reports with gaps. Input and incompatible
baseline errors return a nonzero exit code. It writes only to stdout/stderr;
snapshot persistence is an explicit shell redirection by the caller.

Run the synthetic parser, classification, exclusion, and comparison tests with:

```sh
bazel test //tools/coverage_report:report_test
```
