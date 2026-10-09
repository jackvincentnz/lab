# Bazel contribution guidelines

- Load rules from the wrappers in `tools/bazel/`, such as
  `//tools/bazel:java.bzl` and `//tools/bazel:js.bzl`, so that shared defaults
  and upstream changes are handled in one place. Add a wrapper when a rule is
  missing, and keep its upstream load inside the wrapper module.
- Prefer shorthand labels when the target name matches the last component of
  the package path, such as `//projects/organizer/e2e` instead of
  `//projects/organizer/e2e:e2e`, so that labels stay short and consistent.

## Java BUILD files

[Gazelle](../gazelle.md) writes the `srcs`, `deps`, and `exports` of Java
targets, and the runner and JUnit runtime deps of tests.

- Run `aspect gazelle` after you add, move, or delete Java files or change
  imports, so that BUILD files match the code and the CI check passes.
- Leave the attributes Gazelle writes to Gazelle, so that the next run does not
  undo your edit. Set the others, such as `tags`, `visibility`, `timeout`, and
  `env`, yourself; Gazelle keeps them.
- Declare dependencies used only at runtime, such as JDBC drivers, Spring
  starters, and test resources, in `runtime_deps` as a plain list, so that
  Gazelle keeps them. Gazelle removes from `deps` anything no source imports.
- Generate code into its own Java package, and map it in the root
  `BUILD.bazel` with `# gazelle:resolve`, so that imports of it resolve to the
  generating target.

See [testing](testing.md#bazel-test-targets) for test targets, and
[Bazel](../bazel.md) for how outputs, dependency sources, and the sandbox work
in this repository.
