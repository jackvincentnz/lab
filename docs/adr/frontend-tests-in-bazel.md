---
status: Accepted
date: 2026-10-23
owner: Jack Vincent
---

# Front-end tests in Bazel

## Context

The goal is fast CI feedback: a low median CI time. Two cases describe it: a change to one
front-end app, where only that app's tests re-run, and a change that re-runs every test.

CI runs all tests in one Bazel invocation on a 4-CPU GitHub runner. Bazel assumes each test uses
one CPU and starts as many tests as it has CPUs. Vitest breaks that assumption in two ways:

- **Workers.** By default Vitest starts a worker for each spare CPU. The mops app tests used about
  three CPUs while Bazel counted one, so Bazel ran other tests beside them and overloaded the
  runner. The mops tests slowed from about 19s to between 31s and 51s, close to the 60s limit for
  a small test. `//projects/mops/app:test_run` is tagged `exclusive` to avoid this, so Bazel runs it
  alone after every other test has finished.
- **Isolation.** By default Vitest isolates each test file: it imports every module again and
  builds a new jsdom page for each file. In the mops app this setup is about 73% of Vitest's time.
  The tests themselves are about 14%.

When every test re-runs, the Java and Testcontainers tests use 1,400 to 1,850 CPU-seconds. The
Coverage step is limited by that total work, not by one slow test. Between identical runs, runner
speed changes the Coverage step by about 70 seconds either way.

We measured each option in CI. The mops app has 17 test files today; "5×" copies each file four
times to show how an option scales. Each cell gives the Coverage step time, then the time of the
mops test target.

| Option                                 | Mops change, today | Mops change, 5×    | All tests, today | All tests, 5×    |
| -------------------------------------- | ------------------ | ------------------ | ---------------- | ---------------- |
| `exclusive`, default workers (current) | 35–50s, 13–21s     | 131–133s, 95–97s   | 384–515s, 12–20s | 483–575s, 69–96s |
| Default workers, not exclusive         | –                  | –                  | 478–509s, 31–39s | –                |
| One worker                             | 46–62s, 33–42s     | 179–206s, 148–184s | 397–530s, 36–75s | –                |
| One-worker shards (3 today, 8 at 5×)   | 51s, 19–24s        | 93–95s, 28–34s     | 381–523s, 18–35s | 492–604s, 38–52s |
| No isolation, one worker               | 22–34s, 6–8s       | 31–37s, 16–18s     | 436–495s, 7–18s  | 410–479s, 24–26s |

## Decision

Run front-end tests without per-file isolation, on one Vitest worker for each Bazel test, and do
not mark them `exclusive`:

- `bazelVitestConfig` sets `isolate: false`, so a worker imports modules and builds the jsdom page
  once for all its test files.
- `vitest_run` passes `--maxWorkers=1`, so each test uses the one CPU that Bazel counts for it.
  Watch and UI modes keep Vitest's default workers.
- Per-file setup runs from `setupFiles`, which Vitest runs before every file. Testing Library's
  `cleanup()` must be registered there. `@lab/test-utils/setup` registers it when the module first
  loads, and without isolation that covers only the first file in a worker.
- A shared module is mocked once, in a setup file, and its state is reset after each test, as the
  Statsig mock does. A test file does not `vi.mock` a shared module with mock functions declared
  in that file: without isolation, the first file to mock a module keeps its mock for every later
  file in the worker.
- An app switches when its tests pass without isolation. The mops app switches first and drops
  `exclusive`.

To keep each test target under about 30 seconds on one worker in CI, `fe_app` gives its test
target one Bazel shard for every 100 test files once it has more than 100. At about 0.2 seconds
for each file without isolation, a shard takes about 20 seconds. `bazelVitestConfig` maps Bazel's
shard variables onto Vitest's `shard` option, so each shard runs a slice of the test files on one
worker.

This accepts that test files can affect each other through shared modules and global state. A
test can fail because of a file that ran before it in the same worker.

## Consequences

- A mops-only change runs the mops tests in 6 to 8 seconds instead of 13 to 21, and in under 20
  seconds at five times today's size.
- Bazel's CPU count matches what each front-end test uses, so these tests run beside the Java
  tests without overloading the runner and need no special scheduling.
- A change that re-runs every test is not measurably faster or slower. The Java and Testcontainers
  tests set that time.
- Tests must follow the mocking rule. Reviews enforce it until a lint rule does.
- A failure can depend on test order. To find the cause, re-run the target with `--isolate` and
  compare.
- Shard counts grow with the number of test files, not their run time. A suite of slow files can
  pass 30 seconds a shard before it gets another shard.

## Alternatives considered

How Bazel runs the tests:

- **Keep `exclusive`.** Costs about 15 to 20 seconds when every test re-runs, but the time grows
  with the suite (about 97 seconds at five times today's size), and no other test runs meanwhile.
- **Remove `exclusive` only.** Overloads the runner. The mops tests took 31 to 39 seconds, and 51
  seconds with two workers.
- **Reserve CPUs with a `cpu:N` tag.** Fails analysis: rules_js copies the tag onto the target's
  file copies, which then conflict with the same copies made by the app's other targets.
- **One worker, with isolation.** Bazel's count is correct, but the target takes 33 to 42 seconds
  today, reached 75 seconds when every test re-ran, and took up to 184 seconds at five times
  today's size.
- **One-worker shards, with isolation.** Bazel's count is correct, and shards run in parallel when
  CPUs are free: 19 to 24 seconds today and 28 to 34 seconds for the slowest of 8 shards at five
  times the size. Every shard still pays the isolation cost and starts Vitest again. Kept as the
  way to split a suite that grows too long on one worker.
- **One test target for each file or folder.** Like a Java test target for each test class, each
  target is cached on its own. Only the changed area's tests re-run if test sources also compile
  in matching separate `ts_project` targets; today all of an app's tests compile together. Each
  target starts Vitest again.
- **Remote test execution.** BuildBuddy executors would run tests off the runner and remove the
  CPU contention. Tests that need Docker still run locally, and executors cost money.
- **A larger runner.** More CPUs reduce contention, but GitHub charges for larger runners.

How the tests run inside Vitest:

- **Isolation only for files that mock modules.** Two Vitest projects: one isolated for files that
  use `vi.mock`, one not isolated for the rest. File-local mocks keep working, at the cost of two
  configurations.
- **Thread pool instead of process pool.** About 10% faster with isolation locally. Not measured
  in CI.
- **happy-dom instead of jsdom.** Builds pages faster but implements less of the DOM. Without
  isolation, page setup is about 7% of the time, so the gain is small.
- **Pre-bundled dependencies.** Vitest's dependency optimizer could cut the import cost of
  Mantine, Apollo, and mantine-datatable, which take 100 to 300 milliseconds each to import
  locally. Without isolation a worker pays that cost once. Not measured.
- **Lighter tests.** Test hooks and logic without rendering where possible, and render the
  smallest component that shows the behavior.
- **Vitest's changed-file selection.** Picks tests from the Git diff. Bazel's cache already skips
  tests whose inputs did not change.

## Reconsider when

- A front-end test shard takes more than about 30 seconds on one worker in CI: lower the test
  files for each shard in `fe_app`, or let that target set its own shard count.
- Order-dependent failures recur, or the mocking rule blocks a needed test: evaluate isolation
  only for files that mock modules.
- An app grows into areas that change independently: evaluate a test target for each area, with
  matching `ts_project` targets.
- The Java tests stop setting the full re-run time, or the runner gets more CPUs: measure the full
  re-run case again.
- Vitest changes its isolation or worker defaults: measure again.

## References

- [Vitest: improving performance](https://vitest.dev/guide/improving-performance).
- [Bazel test encyclopedia](https://bazel.build/reference/test-encyclopedia), for test sizes,
  timeouts, and sharding.
