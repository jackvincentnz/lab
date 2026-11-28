# Renovate

Renovate opens pull requests to keep this repository’s dependencies up to date. Related updates are grouped, and CI refreshes generated lockfiles when needed.

## Configuration

Renovate configuration lives in `renovate.json`. Keep changes small and prefer custom managers only when Renovate does not already understand the dependency source.

## Local Validation

The Renovate config validator runs automatically through pre-commit when `renovate.json` changes.

## Pre-Merge Validation

For end-to-end validation before merging, push the proposed config to a `renovate/reconfigure` branch and open a PR against `main`. Renovate treats that PR as a configuration test path and validates the config before it is merged. Check for a successful `renovate/config-validation` status and inspect the Renovate preview comment.

Use this when changing custom managers, package rules, schedules, or other behavior that needs Renovate itself to parse and exercise the config. See [jackvincentnz/lab#774](https://github.com/jackvincentnz/lab/pull/774) for an example.

## Custom Managers

- Give each manager a `managerFilePatterns` pattern that matches every file of its kind instead of a list of files, so that a new file needs no configuration change.
- Write each `matchStrings` entry to match only the smallest block of text that names the dependency and its version, so that it cannot match unrelated text.
- Group every copy of one version into one update, so that the update lands in one PR.

The existing custom managers cover:

- Docker image references embedded in Java and TypeScript files. A digest-pinned
  reference is found anywhere. An unpinned one is found only where it is passed
  to `DockerImageName.parse` or a Testcontainers constructor, or assigned to an
  `*_IMAGE` constant, so that strings such as `"broker:9092"` are not taken for
  images. Renovate opens a PR that pins the digest of each unpinned reference it
  finds. An unpinned image written anywhere else is not found.
- `rules_jvm_external` Maven BOMs declared in `MODULE.bazel`.
- GitHub release assets used by multitool and the OpenTelemetry Java agent.
- The `rules_playwright` browser version in `MODULE.bazel`, tracked as
  `@playwright/test` so that it updates in the same PR as the npm package.

## References

- [Renovate configuration options](https://docs.renovatebot.com/configuration-options/)
- [Renovate custom regex managers](https://docs.renovatebot.com/modules/manager/regex/)
- [Renovate Bazel support](https://docs.renovatebot.com/bazel/)
- [Renovate pre-commit hooks](https://github.com/renovatebot/pre-commit-hooks)
