# External Dependencies

[Renovate](../platform/tools/renovate.md) keeps dependencies up to date.

- Declare a dependency only for a package your code uses directly, so that
  transitive versions come from the packages that need them.
- Leave a security fix in a transitive dependency to the next update of the
  direct dependency that brings it in, so that pins stay rare. Pin it, with an
  npm override in `pnpm-workspace.yaml` or an explicit Maven version, only when
  the fix cannot wait.
- Pin every tool, runtime, and image to an exact version, and images also by
  digest, so that a change behaves the same on every machine and in CI.
- Run the tools that Bazel or the repository provides instead of the host's,
  so that results do not depend on what a machine has installed.

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
