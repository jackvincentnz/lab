# Mops App Test Coverage Plan

## Summary

- First execution step: create `/docs/plans` and add a plan doc there before any app code changes, since that directory did not exist.
- Optimize for real flows first: spend, shell/router, and chat. Defer placeholder-only surfaces (`plan`, `settings`, `counter`) unless they become cheap follow-on coverage wins.
- Use component decomposition as the main testing strategy: extract pure mapping/state helpers and smaller presentational components so tests can focus on stable units instead of large page-level trees.
- Add coverage reporting in this iteration, but do not add a failing CI threshold yet. Treat `~80%` as the directional goal and require each PR to show a qualitative increase from the baseline.

## Implementation Changes

- PR 1, roughly 250-400 lines: planning + test harness + coverage wiring.
  - Create `/docs/plans` and add the plan file.
  - Add Vitest coverage support for the app target and document the Bazel command used to collect it.
  - Expand shared test utilities so app tests can render with the providers the real app uses: Apollo, Mantine, React Query, modals, router, and a complete Statsig mock.
  - Acceptance: `bazel test //projects/mops/app:test_run` stays green, coverage can be generated locally, and the baseline uncovered areas are captured in the plan doc.
- PR 2, roughly 300-450 lines: spend refactor + focused tests.
  - Split `SpendPage.tsx` into pure mapping helpers and a thin data-loading container.
  - Break `SpendTable.tsx` into smaller testable pieces such as column-definition creation, add-row toolbar/action wiring, and validation behavior.
  - Add unit tests for mapping/validation and keep one or two integration tests for the add-line-item flow.
  - Acceptance: spend coverage increases materially without growing the page/table tests into large end-to-end-style fixtures.
- PR 3, roughly 250-450 lines: shell/router decomposition + tests.
  - Split `Shell.tsx` into smaller pieces like header controls, aside toggle/resize handle, and footer/user menu logic.
  - Export route config or a router factory so shell/router behavior can be tested without relying on the app bootstrap.
  - Add tests for page title selection, nav highlighting, chat aside toggle, resize boundary logic, reset-data modal trigger, and Statsig-gated footer links.
  - Acceptance: shell/router paths become covered without needing broad full-app snapshot tests.
- PR 4, roughly 350-500 lines: chat controller split + behavioral tests.
  - Refactor `Chat.tsx` into a controller layer plus smaller UI components for header/view switching, composer/editing state, and message action wiring.
  - Keep network/polling/mutation sequencing in a dedicated hook or controller module so it can be tested with mocked Apollo operations and minimal DOM.
  - Add tests for new chat, selecting history, sending a first message vs appending to an existing chat, editing user messages, retrying assistant messages, and stopping polling when pending assistant messages clear.
  - Acceptance: the largest stateful chat behaviors are covered without one monolithic test file.
- PR 5, roughly 250-450 lines: chat presentation surfaces + cleanup.
  - Split remaining chat rendering into focused components for user messages, assistant states, tool call approvals, and chat history list states.
  - Add tests for pending/failed/completed assistant rendering, markdown content presence, approve/reject tool-call actions, and history loading/error/empty/populated states.
  - Optionally add lightweight tests for `App` provider composition or trivial route pages only if needed to close obvious coverage holes after the main flow work lands.
  - Acceptance: chat coverage becomes broad enough that remaining uncovered code is mostly bootstrap or third-party integration glue.

## Important Interfaces

- No user-facing API changes are planned.
- Internal interfaces likely added:
  - provider-aware app test render helper
  - extracted spend mapping/column builder functions
  - router factory or exported route definition
  - chat controller/hook return shape plus smaller child component props
- Keep these interfaces small and colocated so they support tests without becoming new abstraction layers for their own sake.

## Test Plan

- Baseline and post-PR checks: `bazel test //projects/mops/app:test_run`
- Coverage reporting check: `bazel coverage //projects/mops/app:test_run --combined_report=lcov`
- Key scenarios:
  - spend data mapping, validation, and add-item flow
  - shell nav/title behavior, chat aside open-close, resize bounds, reset modal, gated footer items
  - chat history view, send/edit/retry flows, pending-state polling behavior, tool-call approvals, history empty/error/loading states
- Review rule: each PR should include a short before/after coverage note for the files it touches, not just a pass/fail test run.

## Baseline After Implementation

- Coverage command: `bazel coverage //projects/mops/app:test_run --combined_report=lcov`
- Bazel-native output:
  - merged LCOV report: `bazel-out/_coverage/_coverage_report.dat`
  - per-test LCOV report: `bazel-out/darwin_arm64-fastbuild/testlogs/projects/mops/app/test_run/coverage.dat`
  - HTML review command: `genhtml --branch-coverage --output /tmp/mops-genhtml "$(bazel info output_path)/_coverage/_coverage_report.dat"`
  - HTML review entrypoint: `/tmp/mops-genhtml/index.html`
- Current Vitest coverage baseline:
  - Statements: `82.99%`
  - Branches: `73.28%`
  - Functions: `76.14%`
  - Lines: `83.84%`
- Notable remaining gaps are concentrated in bootstrap and low-value surfaces rather than the main user flows, especially `App.tsx`, `Router.tsx`, `StatsigProvider.tsx`, and the lightly used `counter` route.

## Assumptions And Defaults

- Directional target is about `80%`, but this iteration will not fail CI on a numeric threshold yet.
- “Real flows first” is the chosen scope for the first wave.
- Keep each PR under roughly `500` changed lines by preferring extraction plus focused tests over large rewrites.
- Placeholder pages stay out of the main rollout unless they are a very small follow-up once spend/shell/chat are covered.
