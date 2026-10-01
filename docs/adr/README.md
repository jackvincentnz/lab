# Architecture Decision Records

Architecture decision records capture significant technical decisions, their context, and their
consequences.

## Accepted

- [Java BUILD files](java-build-files.md) generates Java `srcs`, `deps`, and `exports` with Gazelle,
  checked in CI.

## Proposed

- [Edge gateway](gateway.md) decides where platform security boundary controls live and the
  minimum shape of each.
- [Gateway CSRF](gateway-csrf.md) keeps session-backed validation with a fetched token.
