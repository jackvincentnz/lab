# External Dependencies

[Renovate](../platform/tools/renovate.md) keeps dependencies up to date.

- Declare a dependency only for a package your code uses directly, so that
  transitive versions come from the packages that need them.
- Leave a security fix in a transitive dependency to the next update of the
  direct dependency that brings it in, so that overrides stay rare while no
  service runs in production.
- Override the version sooner, with an npm override in `pnpm-workspace.yaml`
  or an explicit Maven version, when the vulnerable package runs on developer
  machines or in CI, such as a build tool or a package with an install script,
  so that a vulnerable package cannot expose local or CI credentials.
- Use an exact version for every tool, runtime, and image, and also a digest
  for images, so that a change behaves the same on every machine and in CI.
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

If CI then reports that an extension "has changed its facts", the data an
extension records in the lockfile, clear Bazel's cached copy of the lockfile
after other Bazel work in this checkout finishes, and regenerate. Bazel before
8.9.0, and 9.x before 9.3.0, can keep stale facts from that cache
([bazelbuild/bazel#29161](https://github.com/bazelbuild/bazel/issues/29161)).
Remove this step once the repository uses a Bazel version with the fix.

```sh
lock="$(bazel info output_base)/MODULE.bazel.lock"
bazel shutdown
rm -f "$lock"
bazel mod deps --lockfile_mode=update
```
