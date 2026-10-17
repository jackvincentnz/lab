# Missing coverage sources

List the tracked production Java and TypeScript files that have no record in a
coverage report. Collect coverage, then run the script from the repository root:

```sh
bazel coverage --combined_report=lcov //...
tools/coverage_report/missing_sources.sh
```

Pass another LCOV file as the first argument to check it instead of
`dist/out/_coverage/_coverage_report.dat`. The script prints one path per line
and a count to stderr.

Test the script with:

```sh
bazel test //tools/coverage_report:missing_sources_test
```
