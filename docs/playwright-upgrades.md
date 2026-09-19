# Playwright upgrades

Playwright is configured in two places and both versions must remain identical:

- `package.json` declares `@playwright/test` for the test runner and client.
- `MODULE.bazel` declares `playwright_version` for `rules_playwright`, which downloads the matching browser binaries for Bazel test runfiles.

Update them together. Updating only the npm dependency makes the newer Playwright client look for a Chromium revision that Bazel has not downloaded, causing Organizer and Mops end-to-end tests to fail with an "Executable doesn't exist" error.

After changing the version, regenerate the module lockfile and run the E2E targets:

```sh
bazel mod deps
bazel test //projects/organizer/e2e:e2e //projects/mops/e2e:e2e
```

Renovate uses a custom manager to keep both declarations in the same dedicated Playwright PR. Playwright is excluded from the general non-major dependency group so this paired update is always reviewed and tested together.
