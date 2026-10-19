#!/usr/bin/env bash

set -euo pipefail

script="$PWD/tools/coverage_report/missing_sources.sh"

# Create a git repository in a new temporary directory and change into it. The
# repository tracks a codecov.yml that ignores learn/**, and each given path as
# an empty file.
enter_repository_tracking() {
  cd "$(mktemp -d "$TEST_TMPDIR/repository.XXXXXX")"
  git init -q
  printf 'ignore:\n  - "learn/**"\n' >codecov.yml
  for path in "$@"; do
    mkdir -p "$(dirname "$path")"
    touch "$path"
  done
  git add .
}

# Write report.dat, an LCOV report with one SF: record per given path.
write_report_covering() {
  for path in "$@"; do
    printf 'SF:%s\nend_of_record\n' "$path"
  done >report.dat
}

# Run the script with the given arguments and keep its stdout, stderr, and exit
# code in variables of those names.
run_missing_sources() {
  exit_code=0
  "$script" "$@" >"$TEST_TMPDIR/stdout" 2>"$TEST_TMPDIR/stderr" || exit_code=$?
  stdout=$(<"$TEST_TMPDIR/stdout")
  stderr=$(<"$TEST_TMPDIR/stderr")
}

# Fail the calling test case when the actual value differs from the expected.
assert_equals() {
  local description="$1" expected="$2" actual="$3"
  if [[ "$actual" != "$expected" ]]; then
    printf 'FAIL %s: %s\nexpected:\n%s\nactual:\n%s\n' "${FUNCNAME[1]}" "$description" "$expected" "$actual" >&2
    exit 1
  fi
}

productionSources_withoutRecord_areListed() {
  enter_repository_tracking libs/a/src/main/java/A.java app/src/App.tsx
  write_report_covering

  run_missing_sources report.dat

  assert_equals "exit code" 0 "$exit_code"
  assert_equals "stdout" "$(printf 'app/src/App.tsx\nlibs/a/src/main/java/A.java')" "$stdout"
}

productionSource_withRecord_isNotListed() {
  enter_repository_tracking libs/a/src/main/java/A.java
  write_report_covering libs/a/src/main/java/A.java

  run_missing_sources report.dat

  assert_equals "stdout" "" "$stdout"
}

nonProductionSources_areNotListed() {
  enter_repository_tracking \
    libs/a/src/test/java/ATest.java \
    app/src/App.test.tsx \
    app/src/App.stories.tsx \
    app/src/test/setup.ts \
    app/src/__fixtures__/data.ts \
    app/src/vite-env.d.ts \
    app/vite.config.ts
  write_report_covering

  run_missing_sources report.dat

  assert_equals "stdout" "" "$stdout"
}

codecovIgnoredSource_isNotListed() {
  enter_repository_tracking learn/kafka/src/main/java/K.java
  write_report_covering

  run_missing_sources report.dat

  assert_equals "stdout" "" "$stdout"
}

summary_withOneOfTwoRecorded_countsMissingAndTotal() {
  enter_repository_tracking libs/a/src/main/java/A.java libs/a/src/main/java/B.java
  write_report_covering libs/a/src/main/java/A.java

  run_missing_sources report.dat

  assert_equals "stderr" "1 of 2 production sources have no record in report.dat" "$stderr"
}

report_missing_failsWithMessage() {
  enter_repository_tracking libs/a/src/main/java/A.java

  run_missing_sources missing.dat

  assert_equals "exit code" 1 "$exit_code"
  assert_equals "stdout" "" "$stdout"
  assert_equals "stderr" "Cannot read coverage report missing.dat" "$stderr"
}

# Run each case in a subshell, so that its directory change stays inside it.
for test_case in \
  productionSources_withoutRecord_areListed \
  productionSource_withRecord_isNotListed \
  nonProductionSources_areNotListed \
  codecovIgnoredSource_isNotListed \
  summary_withOneOfTwoRecorded_countsMissingAndTotal \
  report_missing_failsWithMessage; do
  ("$test_case")
  echo "PASS $test_case"
done
