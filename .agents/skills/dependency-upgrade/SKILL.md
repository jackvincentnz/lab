---
name: dependency-upgrade
description: Use when upgrading a dependency, pinning a version for a security alert, or fixing a failing Renovate PR.
---

# Dependency upgrade

Follow the [dependency guidelines](../../../docs/contributing/dependencies.md).

1. Find the version change. For a failing PR, diff the manifests and lockfiles
   against `origin/main` to find which direct or transitive version change
   causes the failure. For a security alert, read the package and patched
   version from `gh api repos/jackvincentnz/lab/dependabot/alerts/<n>`.
2. Read the release notes or migration guide for each version that you skip
   over. When they do not explain a failure, read the upstream source (see
   [dependency sources](../../../docs/bazel.md#dependency-sources)).
3. Regenerate the lockfiles with the
   [lockfile commands](../../../docs/contributing/dependencies.md#lockfiles).
