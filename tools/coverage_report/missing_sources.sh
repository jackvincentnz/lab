#!/usr/bin/env bash

# List tracked production sources that have no SF: record in an LCOV report, so
# that files no test measures are visible beside the coverage percentage.
#
# Usage, from the repository root:
#   tools/coverage_report/missing_sources.sh [lcov-report]

set -euo pipefail

# Compare paths byte by byte, so that sort and comm agree on the order.
export LC_ALL=C

# Count non-empty lines, so that an empty list counts as zero.
count_lines() {
  grep -c . <<<"$1" || true
}

report="${1:-dist/out/_coverage/_coverage_report.dat}"

if [[ ! -f "$report" || ! -r "$report" ]]; then
  echo "Cannot read coverage report $report" >&2
  exit 1
fi

production_pathspecs=(
  ':(glob)**/src/main/**/*.java'
  ':(glob)**/src/**/*.ts'
  ':(glob)**/src/**/*.tsx'
)

# The test, story, and generated files that tools/bazel/vitest/coverage.ts
# leaves out of frontend coverage.
excluded_pathspecs=(
  ':(glob,exclude)**/test/**'
  ':(glob,exclude)**/__tests__/**'
  ':(glob,exclude)**/__fixtures__/**'
  ':(glob,exclude)**/__generated__/**'
  ':(glob,exclude)**/*.test.*'
  ':(glob,exclude)**/*.spec.*'
  ':(glob,exclude)**/*.stories.*'
  ':(glob,exclude)**/*.d.ts'
)

# Codecov ignores these paths, so a missing record there is expected. Reads the
# top-level ignore list of codecov.yml, one quoted glob per item:
#   ignore:
#     - "learn/**"
codecov_ignore_globs=$(sed -n '/^ignore:/,/^[^ -]/s/^ *- *"\(.*\)"$/\1/p' codecov.yml)
while read -r glob; do
  if [[ -n "$glob" ]]; then
    excluded_pathspecs+=(":(glob,exclude)$glob")
  fi
done <<<"$codecov_ignore_globs"

production_sources=$(git ls-files -- "${production_pathspecs[@]}" "${excluded_pathspecs[@]}" | sort)

covered_sources=$(sed -n 's/^SF://p' "$report" | sort -u)

missing_sources=$(comm -23 <(printf '%s\n' "$production_sources") <(printf '%s\n' "$covered_sources"))

if [[ -n "$missing_sources" ]]; then
  printf '%s\n' "$missing_sources"
fi
echo "$(count_lines "$missing_sources") of $(count_lines "$production_sources") production sources have no record in $report" >&2
