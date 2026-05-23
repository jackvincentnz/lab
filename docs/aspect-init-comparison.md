# aspect_init vs lab — Bazel Setup Comparison

Comparison of Aspect's current `aspect init` boilerplate, (generated 2026-05-22) against this repo. Items ordered by adoption value.

---

## TIER 1 — Highest value

### 1. Bazel version: 7.7.1 → 9.0.0

- `aspect_init`: `9.0.0`
- `lab`: `7.7.1`
- Bazel 9 is the current release line. Most rule\_\* modules now target it. Upgrade unlocks bzlmod-only features and newer rules versions.
- **Effort**: Medium. Requires bumping `.bazelversion`, regenerating `MODULE.bazel.lock`, and dealing with deprecations in your bazelrc presets (your `.aspect/bazelrc/bazel7.bazelrc` is version-pinned).

### 2. `tools/preset.bazelrc` model (replaces `.aspect/bazelrc/*.bazelrc`)

- `aspect_init` ships a single generated `tools/preset.bazelrc` (~13KB) imported from `.bazelrc`, plus `tools/java17.bazelrc`.
- `lab` imports seven hand-curated presets from `.aspect/bazelrc/` (bazel7, convenience, correctness, debug, java, javascript, performance).
- Aspect's current direction is the single generated preset — easier to keep in sync with their recommended defaults via `aspect configure` / regen.
- **Tradeoff**: You lose the easy customization of editing individual presets; you gain "matches upstream" by default. Consider adopting only if you want to track Aspect defaults closely.

### 3. `bazel_env` + `tools/BUILD` pattern

- `aspect_init` uses `aspect_bazel_lib`'s `bazel_env` rule to pin pnpm/node/java/etc. and expose them on PATH via `.envrc`.
- `lab` already partly adopted this (commit `793d638 build: setup bazel_env.bzl for consistent pnpm`) — worth verifying parity with `aspect_init/init/BUILD` (1486 bytes) which has the canonical setup.

### 4. `aspect_rules_lint` integration (linting via Bazel aspects)

- `aspect_init` wires `aspect_rules_lint` 2.0.0 with PMD (Java), shellcheck, eslint as Bazel aspects.
  - `.bazelrc`: `common:lint --aspects=//tools/lint:linters.bzl%shellcheck` (etc.)
  - `tools/lint/` defines aspects; `pmd.xml` at root configures Java rules.
- `lab` has no `aspect_rules_lint` dep. Linting is run via `.pre-commit-config.yaml` (separate tooling, not Bazel-aspected).
- **Why adopt**: Bazel aspects mean lint runs incrementally on changed targets, gets cached remotely, and can be CI-enforced via `bazel build --config=lint //...`. Pre-commit only catches what's staged locally.
- **Effort**: Medium. Add `aspect_rules_lint` to `MODULE.bazel`, copy `tools/lint/`, copy `pmd.xml`.

### 5. `MODULE.aspect` file + Aspect Workflows enablement

- `aspect_init` ships `MODULE.aspect` declaring `aspect_rules_lint` as an AXL dependency (Aspect Workflows extension).
- `lab` has no `MODULE.aspect`.
- Only matters if you plan to use Aspect Workflows (their CI/cloud product). Skip otherwise.

---

## TIER 2 — Useful quality-of-life

### 6. `REPO.bazel`

- `aspect_init` has `REPO.bazel` with `ignore_directories(["**/node_modules"])`.
- `lab` uses `.bazelignore` with explicit paths.
- `REPO.bazel` is the modern, glob-capable replacement. Lower maintenance.

### 7. `.devcontainer/`

- `aspect_init`: Dockerfile (Ubuntu + direnv + bazelisk) + `devcontainer.json` (post-create runs `direnv allow` then `bazel run tools:bazel_env`).
- `lab`: missing.
- Adopt if you ever use Codespaces or VSCode dev containers.

### 8. `.vscode/` workspace settings

- `aspect_init` configures: Bazel LSP, buildifier path (resolved via bazel_env), Prettier path, recommended extensions.
- `lab`: missing (has `.ijwb/` for IntelliJ instead).
- Worth adding if anyone on the team uses VSCode.

### 9. `.github/workflows/renovate-repin.yml`

- `aspect_init` automates re-pinning maven_install.json + MODULE.bazel.lock after Renovate bumps.
- `lab` has `renovate.json` but no auto-repin workflow → Renovate PRs probably fail/stall on lockfile mismatch.
- **High value if you use Renovate**. Low effort.

### 10. `githooks/` (Bazel-driven pre-commit format)

- `aspect_init` has `githooks/pre-commit` that runs `bazel run //tools/format` on staged files, plus `githooks/check-config.sh` as a workspace status command that warns if `core.hooksPath` isn't set.
- `lab` uses the `pre-commit` framework (`.pre-commit-config.yaml`) instead — more portable, less Bazel-coupled.
- **Recommendation**: keep `lab`'s approach. The aspect_init approach is more "Bazel-native" but `pre-commit` is the broader-ecosystem standard.

### 11. `tools/downloader.cfg`

- `aspect_init` configures Bazel's HTTP downloader (mirror/cache hints — see [Aspect blog post linked in the file](https://blog.aspect.build/configuring-bazels-downloader)).
- `lab`: missing.
- Useful for hermetic/cached downloads, especially in CI.

### 12. `tools/workspace_status.sh` for stamping

- `aspect_init` has a stamp script wired via `common:release --stamp --workspace_status_command=./tools/workspace_status.sh`.
- `lab` has `tools/bazel/output_workspace_status.sh` — likely equivalent but worth a parity check.

### 13. `pmd.xml` (Java code quality)

- Only relevant if you adopt item #4 (`aspect_rules_lint`).

---

## TIER 3 — Minor / informational

### 14. `.gitattributes`

- `aspect_init` marks `maven_install.json` as `linguist-generated=true` (collapses it in GitHub diffs) and `githooks/pre-commit` as `rules-lint-ignored=true`.
- Trivial to copy.

### 15. `.shellcheckrc`

- `aspect_init` has a placeholder. Empty stub; only matters if you use shellcheck.

### 16. `README.bazel.md`

- `aspect_init` documents formatting, linting, npm integration, stamping, tools setup. Useful onboarding doc — could be lifted with light edits.

### 17. Node version

- `aspect_init` `.nvmrc`: `22.21.1`
- `lab`: no `.nvmrc`; `package.json` engines: `>=24.13.0 <25`; `MODULE.bazel` likely pins via `bazel_env`
- `lab` is ahead — no action needed except verifying the `bazel_env` pin matches `package.json` engines.

### 18. Module deps `aspect_init` has and `lab` lacks

Cross-check whether any are worth pulling in:

- `bazel_skylib` — utility macros, almost always useful
- `buildifier_prebuilt` — `bazel run @buildifier_prebuilt//:buildifier` to format BUILD files (you have buildifier via pre-commit already)
- `rules_shell` — proper `sh_binary`/`sh_test` rules (newer than the builtin)
- `gazelle` 0.47.0 — BUILD file generation; only useful if you have Go or want auto-gen for protos
- `protovalidate`, `rules_foreign_cc`, `bazel_jar_jar` — skip unless you need them
- `rules_multitool` — manages binary tool versions per-platform; complements `bazel_env`

---

## What `lab` has that `aspect_init` doesn't (don't lose these)

- `aspect_rules_jest`, `aspect_rules_swc`, `aspect_rules_cypress` — JS/TS test stack
- `rules_spring`, `contrib_rules_jvm` — Spring Boot support
- OCI pulls pre-configured (distroless_java, nginx, debian)
- Custom domain `.bzl` macros: `tools/bazel/{fe,java,js,pkg,ts,react}.bzl` + `dgs/`, `jest/`, `oci/`, `storybook/`, `vite/`, `vitest/`
- `tools/bazel/deliver_{all,changed}.sh` — artifact delivery scripts
- Full TypeScript monorepo: `tsconfig.{base,node,react}.json` + per-project tsconfigs
- BuildBuddy remote cache config in CI
- `.commitlintrc.json`, `.editorconfig`, `.prettierignore`
- `.ijwb/` IntelliJ Bazel project files

---

## Suggested adoption order

1. **`renovate-repin.yml`** — quick win, unblocks Renovate (likely already broken silently)
2. **`REPO.bazel`** — trivial migration from `.bazelignore`
3. **`tools/downloader.cfg`** — small file, hermetic-download win
4. **`aspect_rules_lint` + `tools/lint/` + `pmd.xml`** — biggest code-quality lever
5. **`.devcontainer/` + `.vscode/`** — onboarding/IDE wins
6. **Bazel 9 upgrade** — defer until rules\_\* deps you care about have caught up; biggest yak-shave
7. **`tools/preset.bazelrc` model** — only if you want to track Aspect defaults; otherwise stay with your modular presets
