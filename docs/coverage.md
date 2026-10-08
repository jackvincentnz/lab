# Coverage

Coverage helps us find code that tests do not exercise and choose where more
testing would be useful. Local reports give feedback while developing; CI and
Codecov make that feedback available when reviewing a change. Use coverage
alongside the [testing guidelines](contributing/testing.md), test assertions,
and review of the behavior being changed.

## Collection and reporting

Run coverage from the repository root:

```sh
bazel coverage --combined_report=lcov //...
```

The full run needs Docker for tests tagged `requires-docker`; see
[validation setup](../CONTRIBUTING.md#validating-a-change).

Bazel runs tests with coverage instrumentation and merges their reports into
`dist/out/_coverage/_coverage_report.dat`. Java tests use Bazel's JaCoCo
instrumentation. Frontend Vitest tests use V8 coverage remapped to repository
TS/TSX sources. See the [frontend coverage guide](../tools/bazel/vitest/README.md)
for its configuration, exclusions, and collection checks. Coverage output is
cached with test results; the Bazel preset fetches coverage outputs on remote
cache hits so the combined report can include them.

[`.bazelrc`](../.bazelrc) excludes tests tagged `no-coverage` from coverage,
including the end-to-end suites. CI runs those tests separately, then collects
coverage for the remaining tests and uploads the combined LCOV report to
[Codecov](https://codecov.io/gh/jackvincentnz/lab). Upload errors fail CI.
The [workflow](../.github/workflows/main.yml) defines these steps.

[`codecov.yml`](../codecov.yml) configures project and patch comparisons, PR
comments, and component views for Mops app, Mops service, Organizer, and shared
libraries. These views slice the same upload; they do not collect more files.
Codecov ignores `learn/**`. The existing project status compares with the base
commit using an automatic target and a one-percentage-point tolerance; the
patch status is informational.

## What a report establishes

A line hit means the instrumented tests executed that line. A zero-hit line
identifies measured code they did not execute. Branch coverage, where the
collector supplies it, helps identify paths those tests missed. None of these
prove that assertions checked the right outcome, all edge cases were tested,
or the application works as a whole.

The percentage describes the code present in the report after exclusions,
not every source file in the repository. An absent file is different from a
measured file with zero hits. Target selection, instrumentation, runfiles,
source mapping, and exclusions all affect what is measured. Empty baseline
records with `LF:0` add no executable lines. A successful test run or an LCOV
file's existence alone does not establish that collection is complete or
correct. Check file paths and source lines when a result looks surprising.

There is no single reasonable percentage for every part of this repository.
Use the consequence of failure, the logic's complexity, and the behavior
changed to decide where tests are needed. Critical decisions and error paths
deserve focused assertions even when the overall percentage is high. Coverage
is feedback for that judgment; adding execution solely to raise a number can
leave the behavior poorly tested.

## Local feedback

Start with the affected targets for a faster development loop. For example:

```sh
bazel coverage --combined_report=lcov //projects/mops/app:test_run
bazel coverage --combined_report=lcov //projects/mops/service/src/test/java/lab/mops/core/api/gql/lineitem:lineitem
```

Each invocation produces a combined report for its selected targets, not a
repository-wide result. Check the `--instrumentation_filter` Bazel prints too:
it limits the source packages instrumented for those tests. Inspect the report
before running another coverage command:

```sh
less dist/out/_coverage/_coverage_report.dat
```

Find the changed file's `SF:` record and compare its `DA:<line>,<hits>` records
with the source and your diff. Missing files need a collection investigation;
zero-hit lines need a decision about whether a meaningful test should exercise
them. Add assertions for important missing cases, rerun the affected targets,
and check that the expected lines are now exercised. Then run the broader
validation required by [Contributing](../CONTRIBUTING.md#validating-a-change).

Use Codecov's file, patch, and component views during PR review to investigate
changes in reported coverage. Consider the report's scope and the tests'
assertions before interpreting a rise or fall as an improvement or regression.
