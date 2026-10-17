#!/usr/bin/env bash

set -euo pipefail

script="$PWD/tools/coverage_report/missing_sources.sh"
failures=0

# Track the given files in a new repository whose report measures only A.java,
# then run the script there. Prints stdout, or stderr with --summary.
run_with_files() {
  local stream=1
  if [[ "$1" == "--summary" ]]; then
    stream=2
    shift
  fi
  cd "$(mktemp -d "$TEST_TMPDIR/repository.XXXXXX")"
  git init -q
  printf 'ignore:\n  - "learn/**"\n' >codecov.yml
  printf 'SF:libs/a/src/main/java/A.java\nend_of_record\n' >report.dat
  for file in "$@"; do
    mkdir -p "$(dirname "$file")"
    touch "$file"
  done
  git add .
  if [[ $stream == 1 ]]; then
    "$script" report.dat 2>/dev/null
  else
    "$script" report.dat 2>&1 >/dev/null
  fi
}

check() {
  if [[ "$3" != "$2" ]]; then
    printf 'FAIL %s\nexpected:\n%s\nactual:\n%s\n' "$1" "$2" "$3"
    failures=$((failures + 1))
  fi
}

check "productionSources_withoutRecord_areListed" \
  "$(printf 'app/src/App.tsx\nlibs/a/src/main/java/B.java')" \
  "$(run_with_files libs/a/src/main/java/B.java app/src/App.tsx)"

check "productionSource_withRecord_isNotListed" "" \
  "$(run_with_files libs/a/src/main/java/A.java)"

check "testStoryTypeAndConfigSources_areNotListed" "" \
  "$(run_with_files \
    libs/a/src/test/java/ATest.java \
    app/src/App.test.tsx \
    app/src/App.stories.tsx \
    app/src/test/setup.ts \
    app/src/__fixtures__/data.ts \
    app/src/vite-env.d.ts \
    app/vite.config.ts)"

check "codecovIgnoredSource_isNotListed" "" \
  "$(run_with_files learn/kafka/src/main/java/K.java)"

check "summary_withOneOfTwoMissing_countsBoth" \
  "1 of 2 production sources have no record in report.dat" \
  "$(run_with_files --summary libs/a/src/main/java/A.java libs/a/src/main/java/B.java)"

exit "$failures"
