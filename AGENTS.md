# AGENTS

Keep this file focused on actionable context that improves execution quality and reduces churn.

## Maintenance rule

- Update this file when behavior, invariants, or failure-mode guidance changes.
- Remove stale or historical-only notes that no longer affect decisions.
- Keep entries concise and decision-oriented.
- Keep formatting Prettier-friendly: blank line after headings and before lists.

## Hard rules (Do/Don't)

- Run standard Bazel commands first (for example `bazel test //...`, `bazel mod graph --extension_info=usages`).
- If sandbox blocks a needed command, request escalation approval; do not rewrite command shape to bypass sandbox limits.
- Use default Bazel settings first; use debug flags or custom output roots only after reproducing a failure, and note why.
- Prefer Bazel-driven build/test flows over ad-hoc package-manager wrappers.
- If macros reference crane toolchains directly, configure root `oci.toolchains(name = "...")` with a unique name and import that repo (for example `@lab_oci_crane_toolchains`) to avoid both `bazel mod tidy` warnings and extension repo-name collisions.

## Current repo invariants

- Monorepo using Bazel with Bzlmod (`MODULE.bazel`) and no `WORKSPACE`.
- Primary stacks: Java (Spring Boot 3) and Vite + React + TypeScript.
- Node/pnpm versions: Node `>=20.20.0`, pnpm `>=9 <10` (from `package.json`).
- CI avoids transitive `rules_android` host SDK checks by setting `ANDROID_HOME` to an empty value in `.github/workflows/main.yml` `common_ci` job env.
- CI keeps `jlumbroso/free-disk-space` `android: false` to avoid deleting Android tooling unexpectedly on runner images.
- Local Bazel disk cache is enabled by default; CI explicitly disables disk cache.
- Protobuf alignment is pinned: `protobuf@33.4`, runtime `protobuf-java`/`protobuf-java-util` `4.33.4`, protobuf BOM `4.33.4`.
- `.bazelrc` enables proto toolchain resolution and prefers prebuilt protoc (`--incompatible_enable_proto_toolchain_resolution`, `--@protobuf//bazel/toolchains:prefer_prebuilt_protoc`).

## Solution layout

- `projects/`: applications and services.
- `libs/`: shared libraries.
- `tools/bazel/`: Bazel macros and helper scripts.
- `docs/`: style, tooling, and repo conventions.
- `infra/`: infrastructure-related code and configs.
- Frontend apps commonly live under `projects/*/*` with `vite.config.ts`, `index.html`, and `src/`.
- Java services commonly use `src/main/java`, `src/test/java`, `src/main/resources`, and `src/main/proto` with `BUILD.bazel`.

## Known failure modes

- Symptom: `No Android SDK apis found ... /usr/local/lib/android/sdk`.
  Cause: `ANDROID_HOME` points Bazel `rules_android` to a host SDK path without installed API platforms.
  Fix: Set CI `ANDROID_HOME` to an empty value so `rules_android` creates an empty SDK repo for non-Android builds.
- Symptom: `npm_package_store` `UnresolvedSymlink` conflicts for first-party packages.
  Cause: Same peer package resolved in multiple peer contexts.
  Fix: Align peer dependency versions across importers.
- Symptom: `bazel run @pnpm//:pnpm ... install --lockfile-only` updates files outside workspace.
  Cause: pnpm runs in Bazel execroot by default.
  Fix: Always pass `--dir $PWD` for lockfile operations.
- Symptom: `VerifyError` in `ProtobufSchema.toProtoFile` with `kafka-protobuf-serializer`.
  Cause: `io.confluent:kafka-protobuf-serializer:7.9.5` incompatible with protobuf runtime `4.33.4`.
  Fix: Use `io.confluent:kafka-protobuf-serializer:8.0.0`.
- Symptom: DGS schema/resource duplication in test runtime classpath.
  Cause: `contrib_rules_jvm` `java_test_suite` propagates `resources` through generated helper libs.
  Fix: Move schema files to a dedicated `test_resources` `java_library` in `runtime_deps`, not suite-level `resources`.
- Symptom: Missing `McpToolset.fromServer(...)` at compile time.
  Cause: `com.google.adk:google-adk(-dev):0.5.0` does not provide that API.
  Fix: Use `new McpToolset(SseServerParameters...)` and pass toolset to `LlmAgent.builder().tools(...)`.
- Symptom: Starlark error when using `Args.add_all(..., map_each=...)`.
  Cause: `map_each` is not a top-level function.
  Fix: Define `map_each` as a top-level Starlark function.

## Execution defaults

- Common command: `bazel build //...`.
- Common command: `bazel test //...`.
- Target pattern: `bazel <command> //<PATH_TO_PROJECT>:<TARGETS>`.
- Commit messages follow Conventional Commits (`docs/style.md`).

## Recent changes

- 2026-02-13: Refactored `AGENTS.md` from timeline notes into a compact playbook (`Hard rules`, `Invariants`, `Failure modes`, `Execution defaults`).
- 2026-02-13: Documented CI Android handling: keep `android: false` and set `ANDROID_HOME` empty for non-Android Bazel runs.
- 2026-02-13: Documented Bazel sandbox/escalation command policy.
- 2026-02-17: Migrated Jest to v30, removed Node 20 workaround patch/env guidance, and documented pnpm v9 `onlyBuiltDependencies` + Cypress/Testcontainers teardown guidance.
- 2026-02-18: Migrated `@opentelemetry-javaagent` from `WORKSPACE` to `MODULE.bazel` and removed `WORKSPACE` for Bazel 8 readiness.
- 2026-02-18: Added root `oci.toolchains(name = "lab_oci")` and switched macros to `@lab_oci_crane_toolchains` to keep `bazel mod tidy` warning-free without custom resolver rules.
