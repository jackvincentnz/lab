# Architecture Decision Records

Architecture decision records capture significant technical decisions, their context, and their
consequences.

## Adding a record

Copy [the template](template.md) to a kebab-case file named after the decision, such as
`gateway-csrf.md`. List it under Proposed here, and move it to Accepted when it is adopted. The
documentation site groups records by the `status` in their front matter, so update it too. List
the project and library folders the decision is about in `affects`, such as `projects/gateway`, so
that their pages link to it; leave it empty for a decision about the whole repository.

## Accepted

- [Java BUILD files](java-build-files.md) generates Java `srcs`, `deps`, and `exports` with Gazelle,
  checked in CI.
- [Chat work delivery](chat-work-delivery.md) runs chat work after commit and marks failed work
  `FAILED` instead of leaving it `PENDING`.
- [Front-end tests in Bazel](frontend-tests-in-bazel.md) runs Vitest without per-file isolation on
  one worker for each Bazel test, so front-end tests run beside other tests.

## Proposed

- [Edge gateway](gateway.md) decides where platform security boundary controls live and the
  minimum shape of each.
- [Gateway CSRF](gateway-csrf.md) keeps session-backed validation with a fetched token.
