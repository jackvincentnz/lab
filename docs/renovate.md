# Renovate

## Configuration

Renovate configuration lives in `renovate.json`. Keep changes small and prefer custom managers only when Renovate does not already understand the dependency source.

## Local Validation

The Renovate config validator runs automatically through pre-commit when `renovate.json` changes.

## Pre-Merge Validation

For end-to-end validation before merging, push the proposed config to a `renovate/reconfigure` branch and open a PR against `main`. Renovate treats that PR as a configuration test path and validates the config before it is merged. Check for a successful `renovate/config-validation` status and inspect the Renovate preview comment. If the implementation has its own PR, link the validation PR there; close the validation PR without merging after recording the result. Do not overwrite a reconfigure branch being used by another change.

Use this when changing custom managers, package rules, schedules, or other behavior that needs Renovate itself to parse and exercise the config. See [jackvincentnz/lab#774](https://github.com/jackvincentnz/lab/pull/774) for an example.

## Custom Managers

Use custom managers for dependency sources that Renovate does not already extract. Keep each manager narrowly scoped with `managerFilePatterns`, and prefer matching only the smallest useful block of text.

The existing custom managers cover:

- Docker image references embedded in project files.
- `rules_jvm_external` Maven BOMs declared in `MODULE.bazel`.
- [GitHub release assets](#github-release-assets) for multitool binaries and the OpenTelemetry Java agent.

## References

- [Renovate configuration options](https://docs.renovatebot.com/configuration-options/)
- [Renovate custom regex managers](https://docs.renovatebot.com/modules/manager/regex/)
- [Renovate Bazel support](https://docs.renovatebot.com/bazel/)
- [Renovate pre-commit hooks](https://github.com/renovatebot/pre-commit-hooks)

## GitHub Release Assets

Two custom regex managers cover the nine Aspect CLI, buildozer, and ibazel binaries in `tools/tools.lock.json` and the `opentelemetry-javaagent` `http_jar` in `MODULE.bazel`. Keep each URL and its adjacent `sha256` together, preserving the field order matched in `renovate.json`.

These managers use `github-release-attachments`, with the complete release tag (including `v`) as `currentValue` and the bare SHA-256 as `currentDigest`. The datasource identifies the current asset by its checksum, then resolves the corresponding asset in the new release. Each platform binary is extracted separately so its checksum stays paired with its own URL. `github-releases` alone does not provide artifact checksums.

The replacement templates require a nonempty, changed checksum before updating either field. This guards against both missing target assets and the attachment datasource returning the old checksum when it cannot identify the current asset. Renovate reports an update failure instead of writing a new URL with the old checksum. A release that legitimately reuses identical bytes also needs manual verification; verify the asset before changing its pin.

The upstream [rules_multitool automation guide](https://github.com/theoremlp/rules_multitool/blob/main/docs/automation.md) describes a separate updater CLI, not a reusable Renovate preset. These managers keep updates in this repository's existing Renovate grouping and repin workflow. Changes to either `MODULE.bazel` or `tools/tools.lock.json` trigger [Renovate Repin](../.github/workflows/renovate-repin.yml), which refreshes the generated Bazel and Maven lockfiles as needed.

When changing these managers, use the Renovate version pinned in `.pre-commit-config.yaml` to verify extraction and replacement, in addition to the config validator:

- Confirm that the lockfile extracts nine dependencies (two Aspect CLI, four buildozer, and three ibazel binaries), and that the Java agent extracts once.
- Resolve each candidate checksum through `github-release-attachments`, then independently download and SHA-256 hash the candidate asset.
- Exercise Renovate's replacement logic and confirm that each URL and checksum change together, preserving platform fields and unrelated content.
- Confirm that a missing or unchanged replacement checksum rejects a version update.
- Complete the [pre-merge validation](#pre-merge-validation) with the hosted Renovate app and record its result in the PR.
