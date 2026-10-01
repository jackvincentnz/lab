# Bazel contribution guidelines

- Load rules from the wrappers in `tools/bazel/`, such as
  `//tools/bazel:java.bzl` and `//tools/bazel:js.bzl`, so that shared defaults
  and upstream changes are handled in one place. Add a wrapper when a rule is
  missing, and keep its upstream load inside the wrapper module.
- Prefer shorthand labels when the target name matches the last component of
  the package path, such as `//projects/organizer/e2e` instead of
  `//projects/organizer/e2e:e2e`, so that labels stay short and consistent.

See [Bazel](../bazel.md) for how outputs, dependency sources, and the sandbox
work in this repository.
