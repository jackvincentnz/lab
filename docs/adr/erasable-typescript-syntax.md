# Erasable TypeScript syntax

Date: 2026-09-24

## Decision

Enable `erasableSyntaxOnly` in [the shared configuration](../../tsconfig.base.json).
The root IDE config, browser targets, Node targets, and generated GraphQL clients
inherit it without exceptions. This resolves [issue #730](https://github.com/jackvincentnz/lab/issues/730).

The flag rejects TypeScript constructs that require runtime transforms, including
enums, runtime namespaces, and parameter properties. It complements the existing
`verbatimModuleSyntax` setting; see the
[TypeScript reference](https://www.typescriptlang.org/tsconfig/erasableSyntaxOnly.html).
SWC and Vite still handle transpilation and bundling; this decision does not imply
that TSX or every configured language feature can run directly in Node.

## Compatibility evidence

- Evaluated on top of `20c6ab4c` with the repository-pinned TypeScript 7.0.2.
- Inspected the 104 tracked TypeScript/TSX files and compiled all Bazel targets.
  No source refactors or selective opt-outs were needed.
- Built and type-checked all four generated client-preset files for each of Mops,
  Organizer tasklist, and Organizer journal. The existing Mops output represents
  `ChatMessageStatus`, `ChatMessageType`, and `ToolCallStatus` as string unions,
  not TypeScript enums. The current generator configuration needs no changes.
- Confirmed the generated-client `TsProject` action consumes the updated shared
  config, including during declaration-only compilation.
- `bazel test //...` passed all 146 tests locally (42 executed, 104 cached),
  including Docker integration and end-to-end tests. Docker was available;
  no `requires-docker` tests were excluded.
- `bazel build //...` and `pre-commit run --all-files` passed.

Future source or generator changes must satisfy the same compiler check. Use the
[contribution guide](../../CONTRIBUTING.md) for setup, validation commands, and CI.
