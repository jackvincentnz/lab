# AGENTS

Stable repo invariants only. Do not add changelog or history entries here.

## Setup

- `bazel run tools:bazel_env` bootstraps repo-local dev tools onto `PATH` (wired up via direnv/`.envrc`); bazelisk pins the Bazel version from `.bazelversion`.

## Test tags

- `requires-docker` / `requires-network` mark testcontainers and e2e tests that need Docker or network access.
- `no-coverage` excludes tests from coverage instrumentation.
- `--config=codex-cloud` filters out `-requires-docker` for Docker-less environments (see `.bazelrc`).

## CI

- Renovate repin: a red X on Renovate PRs from the repin workflow is expected — `tools/ci/renovate_repin.sh` deliberately exits 1 after force-pushing regenerated lockfiles so CI re-runs on the updated branch.

## Invariants

- Protobuf alignment is pinned: the `protobuf` bazel_dep in `MODULE.bazel` must stay aligned with the Java runtime BOM — currently `protobuf@35.1` and `com.google.protobuf:protobuf-bom:4.35.1` (see `docs/protobuf-upgrades.md`).
