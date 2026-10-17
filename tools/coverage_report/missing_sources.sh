#!/usr/bin/env bash

# Print tracked production sources that have no record in an LCOV report, so
# that files no test measures are visible beside the coverage percentage.
#
# Usage, from the repository root:
#   tools/coverage_report/missing_sources.sh [lcov-report]

set -euo pipefail

report="${1:-dist/out/_coverage/_coverage_report.dat}"

# Production Java and TypeScript, without the test, story, and generated files
# that tools/bazel/vitest/coverage.ts leaves out of frontend coverage.
pathspecs=(
  ':(glob)**/src/main/**/*.java'
  ':(glob)**/src/**/*.ts'
  ':(glob)**/src/**/*.tsx'
  ':(glob,exclude)**/test/**'
  ':(glob,exclude)**/__tests__/**'
  ':(glob,exclude)**/__fixtures__/**'
  ':(glob,exclude)**/__generated__/**'
  ':(glob,exclude)**/*.test.*'
  ':(glob,exclude)**/*.spec.*'
  ':(glob,exclude)**/*.stories.*'
  ':(glob,exclude)**/*.d.ts'
)

# Codecov ignores these paths, so a missing record there is expected.
while read -r pattern; do
  pathspecs+=(":(glob,exclude)$pattern")
done < <(sed -n '/^ignore:/,/^[^ -]/s/^ *- *"\(.*\)"$/\1/p' codecov.yml)

export LC_ALL=C
sources=$(git ls-files -- "${pathspecs[@]}" | sort)
missing=$(comm -23 <(echo "$sources") <(sed -n 's/^SF://p' "$report" | sort -u))

if [[ -n "$missing" ]]; then
  echo "$missing"
fi
echo "$(grep -c . <<<"$missing" || true) of $(grep -c . <<<"$sources" || true) production sources have no record in $report" >&2
