# Missing coverage sources

Collect coverage, then run the script from the repository root:

```sh
bazel coverage --combined_report=lcov //...
tools/coverage_report/missing_sources.sh
```

The script prints each tracked production Java and TypeScript file that has no
record in `dist/out/_coverage/_coverage_report.dat`, one path per line, then
`<missing> of <total> production sources have no record in <report>` to stderr.
Pass another LCOV file as the first argument to check it instead.

Test the script with:

```sh
bazel test //tools/coverage_report:missing_sources_test
```
