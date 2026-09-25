# Bazel contribution guidelines

Use the repository's rule wrappers in `tools/bazel/` instead of loading upstream
rules directly. For example, load Java rules from `//tools/bazel:java.bzl` and
JavaScript rules from `//tools/bazel:js.bzl`.

Wrappers give us one place to maintain shared defaults and adapt to upstream
changes without updating every BUILD file. Add a wrapper when a rule is missing;
keep its upstream load inside the wrapper module.
