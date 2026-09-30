# Bazel contribution guidelines

## Rules and targets

Use the repository's rule wrappers in `tools/bazel/` instead of loading upstream
rules directly. For example, load Java rules from `//tools/bazel:java.bzl` and
JavaScript rules from `//tools/bazel:js.bzl`.

Wrappers give us one place to maintain shared defaults and adapt to upstream
changes without updating every BUILD file. Add a wrapper when a rule is missing;
keep its upstream load inside the wrapper module.

- Prefer shorthand labels when the target name matches the last component of
  the package path: use `//projects/organizer/e2e` instead of
  `//projects/organizer/e2e:e2e`.
- Name a package's main runnable target after the package, so
  `bazel run //projects/<name>` works.
- Run `bazel query //<package>:all` before using a target you have not seen,
  instead of guessing its name.
- Call tools through toolchains or `@bazel_tools` executables with
  `ctx.actions.run`, never host binaries such as `/usr/bin/zip`.
- Keep rule and macro changes minimal. Add no defensive branches or helper tools
  unless a failing case needs them.
- When you move or delete a consumer, remove visibility entries that no longer
  have a consumer.

## Tests and the sandbox

The sandbox denies network access by default. Tag the whole `java_test_suite`
with `requires-network` when its tests bind or connect to sockets, including
Testcontainers. A missing tag shows up as `Operation not permitted` or Ryuk
connection errors. Do not work around it with `--spawn_strategy=local` or
`--strategy=TestRunner=local`.

Tag tests that need Docker with `requires-docker` as well. Split a package's
tests into a second suite only to keep that tag off tests that do not need
Docker, because Docker-free hosts filter on it. Do not override test `size`.

## Outputs and sources

Bazel's convenience symlinks live under `dist/` (`dist/bin`, `dist/testlogs`),
not `bazel-bin`.

To read a dependency's source, look under
`$(bazel info output_base)/external/`. Maven sources jars are in
`rules_jvm_external++maven+<group_artifact>_sources_<version>` repositories.
Do not search
`/private/var/tmp` recursively; it holds stale versions from other output bases.

Run Bazel from the repository root with its default output base. Changing
`--output_base`, `--output_user_root`, or `--batch` restarts the server and
duplicates the cache.
