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

## Coverage inventory

Coverage includes unimported production modules in each test's runfiles, not just
modules touched by tests. The current inventory is:

| Area              | Reported scope                                                                                 | Remaining gap                                                                                                                                                       |
| ----------------- | ---------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Mops app          | All handwritten runtime modules under `src/`, including the entry point, router, and providers | More behavioral tests can now be prioritized using the `mops-app` Codecov component                                                                                 |
| Tasklist          | `src/tasks/`: `DisplayTasks`, `AddTask`, and `TasksPage`                                       | `src/app/App.tsx` and `src/index.tsx` are outside the existing test dependency graph; [#846](https://github.com/jackvincentnz/lab/issues/846) tracks test expansion |
| Journal           | No frontend runtime test target                                                                | [#846](https://github.com/jackvincentnz/lab/issues/846)                                                                                                             |
| Shared bubbles UI | No runtime test target                                                                         | [#848](https://github.com/jackvincentnz/lab/issues/848)                                                                                                             |

Tests, test helpers, fixtures, stories, generated GraphQL code, declarations,
assets, and build configuration are excluded from measured production lines.
Re-export-only modules and type-only modules have no executable statements.
Mops' `spend-table/types.ts` is explicitly excluded from Vitest discovery because
its empty SWC output has no source mapping. Bazel's combined report can still
contain empty baseline records (`LF:0`) for types, assets, or tests; these add no
lines to the denominator.

## Checking changes to the integration

Do not treat a successful test run or the existence of an LCOV file as proof of
coverage. Inspect `SF:` paths and `DA:<source-line>,<hits>` records:

- `shellState.ts`'s `return Math.max(...)` must have hits from its three unit tests.
- `DisplayTasks.tsx`'s `useQuery` line must have hits. Its click handler currently
  has zero hits because the test only renders the task list.
- Unimported `AddTask.tsx` and `StatsigProvider.tsx` must have real zero-hit lines,
  rather than being absent or represented only by `LF:0`.
- Paths with measured lines must resolve to repository TS/TSX files, without
  generated JS duplicates or sandbox paths. Check line numbers against the source.
- Save the report and rerun the same command. A cached run must reproduce it.
  Also run `--nocache_test_results` to verify another sandbox, comparing paths,
  line numbers, and hit/miss status (execution counts may vary between runs).

These sample expectations should evolve when the application's tests change.
Verify normal tests and frontend production builds after changing the shared TS
macro or Vite configuration.
