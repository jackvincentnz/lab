# Testing guidelines

See [coverage](../coverage.md) for how coverage is collected and how to read it.

## Test pyramid

- Unit test every class with logic, so that most behavior is checked by fast,
  focused tests.
- Use Spring context or Testcontainers tests only for SQL, migrations,
  transactions, sessions, security wiring, and wire protocols such as GraphQL
  and HTTP, so that slow tests cover only what needs real infrastructure.

## Layout

- Write a test as arrange, act, and assert, separated by blank lines, and make
  each step one thing you could name in a few words, so that a reader sees what
  the test does before reading how.

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

- Import `render`, `screen`, `userEvent` and the Vitest API from
  [`@lab/test-utils`](../../libs/test-utils/src/index.ts), so that every
  component renders inside the same Mantine, modal, router and Apollo providers.
  Pass `mockedProvider`, `route` and `path` to `render` for Apollo mocks and
  routing, and `wrapper` for any other provider.
- Build an app's `vite.config.ts` with `mergeConfig(bazelVitestConfig(), ...)`
  from [`tools/bazel/vitest/config.ts`](../../tools/bazel/vitest/config.ts), so
  that test discovery, coverage and the shared setup stay the same in every app.
  Point `setupFiles` at an app setup file only for app-specific mocks, as Mops
  does; `@lab/test-utils/setup` runs before it.
- Mock a module with `vi.mock` only in a setup file, and reset the mock's state
  after each test, as the Mops Statsig mock does, so that a test never sees
  another file's mock. Test files in a worker share loaded modules, so a module
  that an earlier file loaded keeps that file's version of its imports. ESLint
  rejects `vi.mock` and `vi.doMock` in test files.
- Create spies and stubs with `vi.spyOn`, `vi.stubGlobal` and `vi.stubEnv`
  inside a test or a `beforeEach`, because Vitest restores them before every
  test. The shared setup also unmounts rendered components, switches back to
  real timers and clears `localStorage` and `sessionStorage` after each test.
- Reset any other state you change, such as a `vi.fn` implementation or a
  window property, in an `afterEach`, so that a test passes whichever file ran
  before it.
- Make window patches in a setup file safe to repeat, because the setup file
  runs again before each test file against the same window.
