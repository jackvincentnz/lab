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

- Use one `java_test_suite` per package. Split a second suite only to keep the
  `requires-docker` tag off tests that do not need Docker, so that Docker-free
  hosts can still run them.
- Tag a suite `requires-network` when its tests bind or connect to sockets,
  including Testcontainers, so that the sandbox allows it. Tag the whole suite
  rather than splitting it.
