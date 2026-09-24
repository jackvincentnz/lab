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

Use custom managers for dependency sources that Renovate does not already extract. Keep each manager narrowly scoped with `managerFilePatterns`, and prefer matching only the smallest useful block of text.

The existing custom managers cover:

- Docker image references embedded in project files.
- `rules_jvm_external` Maven BOMs declared in `MODULE.bazel`.
- GitHub release assets used by multitool and the OpenTelemetry Java agent.

## References

- [Renovate configuration options](https://docs.renovatebot.com/configuration-options/)
- [Renovate custom regex managers](https://docs.renovatebot.com/modules/manager/regex/)
- [Renovate Bazel support](https://docs.renovatebot.com/bazel/)
- [Renovate pre-commit hooks](https://github.com/renovatebot/pre-commit-hooks)
