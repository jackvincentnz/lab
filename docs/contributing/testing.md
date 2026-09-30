# Testing guidelines

## Test pyramid

Test as much as possible with unit tests. Give every class with logic a unit
test.

Use Spring context or Testcontainers tests only for what needs them: SQL,
migrations, transactions, sessions, security wiring, and wire protocols such as
GraphQL, MCP, and HTTP. Back other tests with in-memory substitutes. Do not add
end-to-end tests that start another project's binary unless the issue asks for
one.

## Java tests

Open a Mops mapper or mutation test, such as
[`LineItemMapperTest`](../../projects/mops/service/src/test/java/lab/mops/core/api/gql/lineitem/LineItemMapperTest.java),
as the reference.

- Extend `lab.test.TestBase`. Generate values inside each test with its
  `randomString()`, `randomId()`, `randomInt()`, and `fixedClock()` helpers.
- Use static state only for infrastructure a Spring context needs, such as a
  Testcontainers container or a stub server.
- Name methods `subject_condition_outcome`, for example
  `login_withWrongPassword_redirectsToError`.
- Lay out each test as arrange, act, and assert, separated by blank lines.
- Prefer two named tests over one test that asserts two cases.
- Use Spring's idiomatic clients, such as `WebTestClient` or `MockMvc`, and
  assert behavior specific to the service, not only health.
- In Postgres-backed tests, compare timestamps with the persisted value. CI runs
  on Linux, where `Instant` has nanosecond precision and Postgres stores
  microseconds.

Use one `java_test_suite` per package. See
[Bazel tests and the sandbox](bazel.md#tests-and-the-sandbox) for tags.

## Code scanning

CodeQL alerts whose only instances are in test code are dismissed as used in
tests. Do not weaken a test to satisfy the scanner.
