# Dependency contribution guidelines

[Renovate](../renovate.md) keeps dependencies up to date. These guidelines cover
manual upgrades, security pins, and fixes to Renovate PRs.

## Versions

- Upgrade to the newest release in the requested line, not its first release.
- Use stable releases. Ask before adopting a beta, release candidate, or
  milestone, and offer a stable alternative.
- If pnpm refuses a version as too new, choose an older release instead of
  adding an exception.

## Declaring dependencies

- Add a Maven artifact or explicit version only when no BOM or starter supplies
  it, such as for a security pin or a proven compatibility gap. Put a comment on
  the same line that says why it exists and when to remove it.
- Prefer a Spring Boot starter over listing the starter and its modules
  together.
- BOMs in `MODULE.bazel` resolve first-wins, so list an override BOM before
  `spring-boot-dependencies`.
- Isolate third-party classpath quirks in a `//third_party/<lib>` wrapper, as
  `//third_party/dgs` does, instead of repeating them in app BUILD files.
- Pin transitive npm packages under `overrides` in `pnpm-workspace.yaml`.
- Never add a transitive npm package to `package.json` or BUILD deps to fix a
  resolution error. Find why the rules_js layout does not resolve it.
- For a security alert, read the patched version from
  `gh api repos/jackvincentnz/lab/dependabot/alerts/<number>` and prefer
  upgrading the parent dependency over a pin.

## Upgrades

- Keep existing behavior. When a flag or API no longer compiles, find out why
  the code used it and preserve that behavior. Look for moved classes in the new
  modules and the migration guide before you replace them.
- When an upgrade breaks a consumer, upgrade or replace the consumer. Do not
  downgrade the dependency the change set out to upgrade.
- List each behavior change in the PR description.
- Run `bazel test //...` before reporting an upgrade as done.

## Lockfiles

Never edit lockfiles by hand or restore them with `git show` or `git checkout`
to hide changes. Regenerate them with the owning tool and commit the result with
the change that caused it:

| Lockfile             | Command                                 |
| -------------------- | --------------------------------------- |
| `maven_install.json` | `REPIN=1 bazel run @maven//:pin`        |
| `MODULE.bazel.lock`  | `bazel mod deps --lockfile_mode=update` |
| `pnpm-lock.yaml`     | `pnpm install --lockfile-only`          |

CI runs with `--lockfile_mode=error`, so a stale `MODULE.bazel.lock` fails there
even when local builds pass. After a rebase that touches any of these files,
regenerate them instead of trusting the textual merge.
