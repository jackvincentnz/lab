# aspect_init vs lab — Bazel Setup Comparison

Comparison of Aspect's current `aspect init` boilerplate, (generated 2026-05-22) against this repo. Items ordered by adoption value.

---

## TIER 1 — Highest value

### 1. Bazel version: 8.7.0 → 9.0.0

- `aspect_init`: `9.0.0`
- `lab`: `8.7.0`
- Bazel 9 is the current release line. Most rule\_\* modules now target it. Upgrade unlocks bzlmod-only features and newer rules versions.
- **Recommendation**: hold at Bazel 8.7 until `rules_spring` is ready for Bazel 9. This repo still depends on `rules_spring` for Spring Boot services, so forcing Bazel 9 early is higher risk than the current delta justifies.
- **Effort later**: Medium. Requires bumping `.bazelversion`, regenerating `MODULE.bazel.lock`, and rerunning the standard Bazel build/test flow against the Spring targets.

### 2. `aspect_rules_lint` integration (linting via Bazel aspects)

- `aspect_init` wires `aspect_rules_lint` 2.0.0 with PMD (Java), shellcheck, eslint as Bazel aspects.
  - `.bazelrc`: `common:lint --aspects=//tools/lint:linters.bzl%shellcheck` (etc.)
  - `tools/lint/` defines aspects; `pmd.xml` at root configures Java rules.
- `lab` has no `aspect_rules_lint` dep. Linting is run via `.pre-commit-config.yaml` (separate tooling, not Bazel-aspected).
- **Why adopt**: Bazel aspects mean lint runs incrementally on changed targets, gets cached remotely, and can be CI-enforced via `bazel build --config=lint //...`. Pre-commit only catches what's staged locally.
- **Effort**: Medium. Add `aspect_rules_lint` to `MODULE.bazel`, copy `tools/lint/`, copy `pmd.xml`.

### 3. `MODULE.aspect` file + Aspect Workflows enablement

- `aspect_init` ships `MODULE.aspect` declaring `aspect_rules_lint` as an AXL dependency (Aspect Workflows extension).
- `lab` has no `MODULE.aspect`.
- Only matters if you plan to use Aspect Workflows (their CI/cloud product). Skip otherwise.

---

## TIER 2 — Useful quality-of-life

### 4. `.devcontainer/`

- `aspect_init`: Dockerfile (Ubuntu + direnv + bazelisk) + `devcontainer.json` (post-create runs `direnv allow` then `bazel run tools:bazel_env`).
- `lab`: missing.
- Adopt if you ever use Codespaces or VSCode dev containers.

### 5. `.vscode/` workspace settings

- `aspect_init` configures: Bazel LSP, buildifier path (resolved via bazel_env), Prettier path, recommended extensions.
- `lab`: missing (has `.ijwb/` for IntelliJ instead).
- Worth adding if anyone on the team uses VSCode.

### 6. `githooks/` (Bazel-driven pre-commit format)

- `aspect_init` has `githooks/pre-commit` that runs `bazel run //tools/format` on staged files, plus `githooks/check-config.sh` as a workspace status command that warns if `core.hooksPath` isn't set.
- `lab` uses the `pre-commit` framework (`.pre-commit-config.yaml`) instead — more portable, less Bazel-coupled.
- **Recommendation**: keep `lab`'s approach. The aspect_init approach is more "Bazel-native" but `pre-commit` is the broader-ecosystem standard.

### 7. `tools/downloader.cfg`

- `aspect_init` configures Bazel's HTTP downloader (mirror/cache hints — see [Aspect blog post linked in the file](https://blog.aspect.build/configuring-bazels-downloader)).
- `lab`: missing.
- Useful for hermetic/cached downloads, especially in CI.

### 8. `tools/workspace_status.sh` for stamping

- `aspect_init` has a stamp script wired via `common:release --stamp --workspace_status_command=./tools/workspace_status.sh`.
- `lab` has `tools/bazel/output_workspace_status.sh` — likely equivalent but worth a parity check.

### 9. `pmd.xml` (Java code quality)

- Only relevant if you adopt item #2 (`aspect_rules_lint`).

---

## TIER 3 — Minor / informational

### 10. `.gitattributes`

- `aspect_init` marks `maven_install.json` as `linguist-generated=true` (collapses it in GitHub diffs) and `githooks/pre-commit` as `rules-lint-ignored=true`.
- Trivial to copy.

### 11. `.shellcheckrc`

- `aspect_init` has a placeholder. Empty stub; only matters if you use shellcheck.

### 12. `README.bazel.md`

- `aspect_init` documents formatting, linting, npm integration, stamping, tools setup. Useful onboarding doc — could be lifted with light edits.

### 13. Module deps `aspect_init` has and `lab` lacks

Cross-check whether any are worth pulling in:

- `buildifier_prebuilt` — `bazel run @buildifier_prebuilt//:buildifier` to format BUILD files (you have buildifier via pre-commit already)
- `gazelle` 0.47.0 — BUILD file generation; only useful if you have Go or want auto-gen for protos
- `protovalidate`, `rules_foreign_cc`, `bazel_jar_jar` — skip unless you need them

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

1. **`tools/downloader.cfg`** — small file, hermetic-download win
2. **`aspect_rules_lint` + `tools/lint/` + `pmd.xml`** — biggest code-quality lever
3. **`.devcontainer/` + `.vscode/`** — onboarding/IDE wins if those environments matter
4. **Bazel 9 upgrade** — defer until `rules_spring` is ready
