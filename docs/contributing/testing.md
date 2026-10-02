# Testing guidelines

## Test pyramid

- Unit test every class with logic, so that most behavior is checked by fast,
  focused tests.
- Use Spring context or Testcontainers tests only for SQL, migrations,
  transactions, sessions, security wiring, and wire protocols such as GraphQL
  and HTTP, so that slow tests cover only what needs real infrastructure.

## Java tests

Use a Mops mapper or mutation test, such as
[`LineItemMapperTest`](../../projects/mops/service/src/test/java/lab/mops/core/api/gql/lineitem/LineItemMapperTest.java),
as the reference.

- Extend `lab.test.TestBase` and generate values inside each test with its
  `random*()` and `fixedClock()` helpers, so that each test owns its data.
  Keep static state for infrastructure a Spring context needs, such as a
  container.
- Name test methods `subject_condition_outcome`, such as
  `login_withWrongPassword_redirectsToError`, so that a failure says what broke.

## Bazel test targets

[Gazelle](../gazelle.md) generates one `java_test_suite` per test package,
named after the directory.

- Annotate a test class that starts Docker containers with
  `@lab.test.RequiresDocker`, so that Gazelle gives it its own target tagged
  `requires-docker` and `requires-network`, and Docker-free hosts can still run
  the rest of the package.
- Tag a suite `requires-network` when its tests bind or connect to sockets, so
  that the sandbox allows it. Tag the whole suite rather than splitting it.
- Put test helpers that several packages share in their own package under
  `src/test`, with no tests, so that other tests depend on its
  `<name>-test-lib` target, such as
  `//libs/test/src/test/java/lab/test:test-test-lib`.
