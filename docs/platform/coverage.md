# Coverage

Coverage shows which code the tests execute, so that reviewers and authors can
see where testing is missing. It does not show that assertions check the right
outcome.

## Collection

CI collects coverage for every test target except the end-to-end suites, which
are tagged `no-coverage` and run in a separate `bazel test` step:

```sh
bazel coverage --combined_report=lcov //...
```

Bazel merges each test's LCOV into `dist/out/_coverage/_coverage_report.dat`.
Java tests use Bazel's JaCoCo instrumentation. Vitest tests use V8 coverage
remapped to TS/TSX sources; the
[frontend coverage guide](../../tools/bazel/vitest/README.md) owns its
configuration and exclusions. Coverage outputs are cached with test results,
and the [Bazel preset](../../tools/preset.bazelrc) fetches them on remote cache
hits so that the combined report stays complete.

The [workflow](../../.github/workflows/main.yml) uploads the report to
[Codecov](https://codecov.io/gh/jackvincentnz/lab) and fails if the upload
fails. [`codecov.yml`](../../codecov.yml) sets the checks:

| Check             | Behavior                                                                             |
| ----------------- | ------------------------------------------------------------------------------------ |
| `codecov/project` | Fails when total coverage drops more than one percentage point from the base commit. |
| `codecov/patch`   | Reports coverage of changed lines; informational only.                               |

Its components for Mops app, Mops service, Organizer, and shared libraries
slice the same upload. Codecov ignores `learn/**`.

## Reading a report

- Treat a zero-hit line as a decision to make, not a test to add, so that new
  tests check behavior instead of raising a number.
- Treat a changed file absent from the report as a collection problem, so that
  it is not mistaken for full or zero coverage. Bazel instruments only the
  packages in the `--instrumentation_filter` it prints, and test selection,
  runfiles, source mapping, and exclusions all decide what is measured.
- Judge a rise or fall against the report's scope and the tests' assertions,
  so that a percentage change is not read as better or worse testing.

No single percentage fits the whole repository. Weigh the consequence of a
failure and the complexity of the logic, and give critical decisions and error
paths focused assertions even when the total is high.

## Local feedback

Collect coverage for the tests of the code you changed, then run
[changed-line coverage](tools.md#changed-line-coverage) to list the changed
lines those tests miss. It does not list a changed file that the report omits;
[missing sources](../../tools/coverage_report/README.md) lists every production
file the report has no record for, so run it to find such a file. A local run
measures only the selected targets, so its count means something only for a
full `//...` collection. Then run the broader
[validation](../../CONTRIBUTING.md#validating-a-change).
