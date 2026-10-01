---
name: dependency-upgrade
description: Use when upgrading a dependency, pinning a version for a security alert, or fixing a failing Renovate PR.
---

# Dependency upgrade

Follow the [dependency guidelines](../../../docs/contributing/dependencies.md).

1. Find the version change. For a failing PR, diff the manifests and lockfiles
   against `origin/main` to find which direct or transitive version change
   causes the failure. For a security alert, read the package, manifest, and
   patched version from
   `gh api repos/jackvincentnz/lab/dependabot/alerts/<n>`.
2. Choose the newest stable release in the line, not the oldest version that
   passes. Do not use pre-releases.
3. Read the upstream source under `$(bazel info output_base)/external` (see
   [dependency sources](../../../docs/bazel.md#dependency-sources)) and the
   release notes or migration guide for each version that you skip over.
4. Keep the existing behavior. Fix the code that uses the dependency instead of
   downgrading it. If you cannot keep the behavior, ask the user.
5. Regenerate the lockfiles with the
   [lockfile commands](../../../docs/contributing/dependencies.md#lockfiles).
   Do not edit lockfiles by hand.
6. Run `bazel test //...`, then [validate](../validate/SKILL.md) the rest of
   the change.

List the behavior changes from the upgrade in the PR's `Change` section, or say
that there are none.
