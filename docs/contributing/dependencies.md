# Dependency contribution guidelines

[Renovate](../renovate.md) keeps dependencies up to date.

- Declare a dependency only for a package your code uses directly, so that
  transitive versions come from the packages that need them.
- Pin a transitive dependency only when a security fix requires it, using an
  npm override in `pnpm-workspace.yaml` or an explicit Maven version, so that
  pins stay rare and easy to remove.

## Lockfiles

Lockfiles are generated. Update them with the owning tool and commit them with
the change that caused them:

| Lockfile             | Command                                 |
| -------------------- | --------------------------------------- |
| `maven_install.json` | `REPIN=1 bazel run @maven//:pin`        |
| `MODULE.bazel.lock`  | `bazel mod deps --lockfile_mode=update` |
| `pnpm-lock.yaml`     | `pnpm install --lockfile-only`          |

CI fails on a stale `MODULE.bazel.lock`. After a rebase that touches a
lockfile, regenerate it instead of keeping the textual merge.
