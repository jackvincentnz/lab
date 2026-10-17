# Architecture Decision Records

Architecture decision records capture significant technical decisions, their context, and their
consequences.

## Adding a record

Copy [the template](template.md) to a kebab-case file named after the decision, such as
`gateway-csrf.md`. List it under Proposed here and in [the sidebar](../_sidebar.md), and move it
to Accepted in both when it is adopted.

## Accepted

- [Java BUILD files](java-build-files.md) generates Java `srcs`, `deps`, and `exports` with Gazelle,
  checked in CI.

## Proposed

- [Frontend BUILD automation](frontend-build-automation.md) generates frontend TypeScript targets
  and deps with Aspect's Gazelle runner, one package per app, around a thinner `fe_app`.
- [Edge gateway](gateway.md) decides where platform security boundary controls live and the
  minimum shape of each.
- [Gateway CSRF](gateway-csrf.md) keeps session-backed validation with a fetched token.
