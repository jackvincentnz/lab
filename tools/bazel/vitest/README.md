# Frontend coverage

Use the same targets for tests and coverage:

```sh
bazel test //projects/mops/app:test_run //projects/organizer/tasklist/src/tasks:test
bazel coverage --combined_report=lcov //projects/mops/app:test_run //projects/organizer/tasklist/src/tasks:test
```

CI runs `bazel coverage --combined_report=lcov //...` and uploads
`dist/out/_coverage/_coverage_report.dat` to Codecov. Each test's native Vitest
LCOV is also available at `dist/testlogs/<package>/<target>/coverage.dat`.
Bazel caches that output with the test result and merges it with Java coverage.

## How the integration works

- `coverage.ts` enables the version-matched V8 provider only when Bazel supplies
  `COVERAGE_OUTPUT_FILE`. Normal tests and production builds do not collect coverage.
- SWC emits source maps matching `ts_project`'s `source_map` setting. During
  coverage, the TS macro also includes original sources in runfiles: Vitest
  discards remapped files that do not exist, even with inline source content.
- Tests and coverage discovery select compiled JS, avoiding duplicate execution
  when original TS is present. Vitest remaps line hits to TS/TSX; the LCOV reporter
  uses the workspace's runfiles root for repository-relative paths.
- Vitest writes directly to `COVERAGE_OUTPUT_FILE`. The rules_js collector keeps
  an existing nonempty report, avoiding its generic V8 conversion of the launcher.
  Cleaning is disabled for Bazel's shared test-output directory.

Coverage includes unimported production modules available in each test's runfiles.
Tests, test helpers, fixtures, stories, generated code, declarations, assets, and
build configuration are excluded from measured production lines. Modules with no
executable statements contribute no lines. Bazel's combined report can contain
empty baseline records (`LF:0`); these add no lines to the denominator.

## Checking changes to the integration

Do not treat a successful test run or the existence of an LCOV file as proof of
coverage. Inspect `SF:` paths and `DA:<source-line>,<hits>` records:

- Check that executed source lines have hits and unexecuted production code has
  real zero-hit lines, including modules no test imports. An empty `LF:0` record
  does not demonstrate coverage of executable code.
- Confirm measured paths resolve to repository TS/TSX files and line numbers
  match the source, without generated JS duplicates or sandbox paths.
- Compare a fresh run (`--nocache_test_results`) with a cached run
  (`--cache_test_results=yes`). Cached output must reproduce the report; fresh
  runs must preserve paths, line numbers, and hit/miss status even when execution
  counts vary.

Verify normal tests and frontend production builds after changing the shared TS
macro or Vite configuration.
