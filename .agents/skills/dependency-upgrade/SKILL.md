---
name: dependency-upgrade
description: Use when upgrading a dependency, fixing a Dependabot security alert, or fixing a failing Renovate PR.
---

# Dependency upgrade

Follow the [dependency guidelines](../../../docs/contributing/dependencies.md).

1. Fetch `origin` and rebase onto `origin/main` first, and drop any update
   `main` already has, so that you fix the current failure.
2. Find the version change. For a failing PR, diff the manifests and lockfiles
   against `origin/main` to find which direct or transitive version change
   causes the failure. For a security alert, read the package and patched
   version from `gh api repos/jackvincentnz/lab/dependabot/alerts/<n>`.
3. Read the release notes or migration guide for each version that you skip
   over. When they do not explain a failure, read the upstream source (see
   [dependency sources](../../../docs/platform/bazel.md#dependency-sources)).
4. Regenerate every lockfile with the
   [lockfile commands](../../../docs/contributing/dependencies.md#lockfiles).
5. Check what the dependency affects, such as by running the app or the tool,
   even when CI is green, because CI proves nothing when no test uses it.
6. When `renovate.json` changes, run the
   [pre-merge validation](../../../docs/platform/tools/renovate.md#pre-merge-validation).
7. For a security alert in a transitive dependency, follow the
   [dependency guidelines](../../../docs/contributing/dependencies.md) on
   overrides, and report which direct dependency should bring the fix.
8. Leave a Renovate PR you cannot fix open as a draft, with a comment that
   names the blocker, because closing it makes Renovate ignore that version.
