#!/usr/bin/env bash

set -euo pipefail

write_summary() {
  if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
    printf "%s\n" "$1" >>"${GITHUB_STEP_SUMMARY}"
  fi
}

run_repins() {
  bazel mod deps --lockfile_mode=update
  REPIN=1 bazel run @maven//:pin
}

changed_lockfiles() {
  ! git diff --quiet -- MODULE.bazel.lock maven_install.json
}

run_repins

if ! changed_lockfiles; then
  write_summary "No repin changes required for \`MODULE.bazel.lock\` or \`maven_install.json\` on the PR merge checkout."
  exit 0
fi

if [[ "${IS_FORK:-false}" == "true" ]]; then
  write_summary "Repin required, but this PR is from a fork so CI cannot push generated lockfiles."
  exit 1
fi

if [[ -z "${REPIN_PUSH_TOKEN:-}" ]]; then
  write_summary "Repin required, but \`REPIN_PUSH_TOKEN\` is missing. Configure it so follow-up workflows trigger."
  exit 1
fi

# Restore tracked files before switching away from the merge checkout.
git checkout -- .
git fetch origin "${GITHUB_HEAD_REF}"
git checkout -B "${GITHUB_HEAD_REF}" "origin/${GITHUB_HEAD_REF}"
run_repins

if ! changed_lockfiles; then
  write_summary "Repin required on merge checkout but source branch has no lockfile diff. Rebase or refresh the PR branch."
  exit 1
fi

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add MODULE.bazel.lock maven_install.json
git commit -m "chore: repin generated lockfiles"
git push origin "HEAD:${GITHUB_HEAD_REF}"

write_summary "Repin commit pushed to \`${GITHUB_HEAD_REF}\`; failing this run intentionally so follow-up CI runs on the updated branch."
exit 1
