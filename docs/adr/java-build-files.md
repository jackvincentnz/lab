# Java BUILD files

## Status

Accepted.

Date: 2026-09-30
Decision owner: Jack Vincent

## Context

Java BUILD files were written by hand, one per Java package. Bazel's strict Java deps fail a build
when a direct dependency is missing, but nothing reports a dependency that is no longer used, so
deps accumulated as code was deleted. The test macro also added assertj, Mockito, JUnit, and
`lab.test` to every suite, whether or not the suite used them.

## Decision

Generate the dependency-bearing parts of Java BUILD files with [Gazelle](../gazelle.md) and the
Java extension from `contrib_rules_jvm`. CI fails when a BUILD file differs from Gazelle's output.

The extension's model sets these conventions:

- Gazelle owns `srcs`, `deps`, and `exports` of Java libraries and tests, and writes `srcs` as
  explicit lists.
- Gazelle writes `runner = "junit5"` and the JUnit runtime deps into every test target that imports
  JUnit 5.
- Libraries export the dependencies whose types appear in their public API, like Gradle's `api`
  configuration, so that callers compile without naming those dependencies.
- People write `runtime_deps` as plain lists. The extension keeps plain lists and replaces other
  expressions.
- Each test package has one `java_test_suite`, named after its directory.
- Test helpers under `src/test` are `java_test_suite` targets with no tests. Other tests depend on
  their `<name>-test-lib` library.
- Test classes that start Docker containers are annotated `@RequiresDocker`. Gazelle gives each one
  its own `java_junit5_test` tagged `requires-docker` and `requires-network`.
- Generated code lives in its own Java package, and the root `BUILD.bazel` maps that package to its
  target.

## Consequences

- Deleting code removes the deps only that code used, and new imports add deps, without editing
  BUILD files.
- BUILD files grow by a line per source file, and every test target repeats the runner and three
  JUnit runtime deps.
- A test split out by `@RequiresDocker` gets the deps of every import in its package, not only its
  own, and so does the suite it was split from.
- A test always depends on the main library of its own Java package, so that library must be
  visible to the test package.
- Gazelle cannot see dependencies reached only at runtime, such as JDBC drivers, `jdbc:tc:` URLs,
  Spring starters, and test resources. They must be in `runtime_deps`, or Gazelle removes them from
  `deps`.

## Alternatives considered

- **Keep writing BUILD files by hand, and check them with `unused_deps`.** The buildtools tool reads
  javac's dependency output and prints buildozer commands, with no configuration. It found 2 unused
  deps where Gazelle removed about 40: it skips the targets the `java_test_suite` macro creates, and
  counts a dependency as used when javac reads any class from it. It never adds deps, and gives no
  CI check.
- **`aspect configure`.** Aspect's bundled Gazelle has no Java language. The Aspect CLI's
  `aspect gazelle` task runs this repository's Gazelle target instead, and is the local command.
- **Keep `glob` sources with `# keep`.** Gazelle still computes deps from every Java file in the
  directory, so this works, but every rule needs the comment. Explicit lists are the extension's
  default and show added and removed files in review.
- **Keep shared test deps in the macro or in constants.** The macro added deps that suites did not
  use, and the extension cannot read `SPRING_TEST_DEPS + [...]` concatenations.
- **Name suites `tests`.** The repository was split between `tests` and the directory name. The
  directory name is the extension's default and gives shorthand labels.
- **Put Docker tests in separate packages.** The extension allows one test directory per Java
  package, so they would need another Java package and lose package-private access.
- **Move test helpers to a `src/testFixtures` source set.** This is the Gradle convention, and the
  extension supports it with `# gazelle:java_testonly true`. Helper-only suites need no files moved.

## Reconsider when

- The Java extension can leave out the runner and JUnit runtime deps: drop them from BUILD files and
  set them in the macro.
- Several packages mix Docker and Docker-free tests and the shared deps slow builds: move the Docker
  tests to their own Java packages.
- Front-end BUILD files are generated too: run the same Gazelle binary and CI check rather than a
  second tool.

## References

- [Gazelle Java extension](https://github.com/bazel-contrib/rules_jvm/tree/main/java/gazelle).
- [Gazelle](https://github.com/bazel-contrib/bazel-gazelle).
- [`unused_deps`](https://github.com/bazelbuild/buildtools/tree/main/unused_deps).
