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

## Frontend tests

Import `render`, `screen`, `userEvent` and Vitest helpers from `@lab/test-utils`.
The shared render composes Mantine and modals, with optional `mockedProvider`
(Apollo mocks), `route` and `path` (memory routing). Supply a `wrapper` for
app-specific providers; Mops keeps Statsig in its local render wrapper.

Import `@lab/test-utils/setup` in app setup files for DOM matchers, cleanup and
browser mocks. App-specific mocks stay in those files. Mops calls
`mockMatchMedia(true)` to retain its media-query behavior; the default is false.

Use `bazelVitestConfig` from `tools/bazel/vitest/config.ts` and `mergeConfig`
for app Vite configuration. Pass test discovery and coverage patterns relative
to the app's Vite root, and set `setupFiles` for app-specific setup. The shared
workspace-root config uses the same defaults with workspace-relative patterns.
Keep proxies, build plugins and app overrides in the app config. Bazel tests
consume compiled `.js` files; TypeScript runfiles exist for coverage remapping
and must not be discovered as a second copy of each test.
