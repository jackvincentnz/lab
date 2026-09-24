# Flatten frontend application targets

Status: Accepted.

## Context

Frontend tools already operate on application-sized module graphs. Applying the
repository's one-package-per-directory pattern to every frontend component adds
TypeScript, SWC and test toolchain startup costs across many small Bazel actions.
Mops already groups production and test compilation broadly, but repeats the
target wiring across app, source and test-support BUILD files.

## Decision

Use one app-root `fe_app` invocation for new frontend applications. Compile
production sources together, tests and app-specific helpers together, and optional
stories separately. Retain generated code as an explicit dependency and keep
application configuration outside the macro. Adopt this in Mops first.

Retain `fe_library` unchanged for existing directory-level library consumers.
New applications use `fe_app`; migrating Organizer and Bubbles is deferred to
#844 and #847. Shared test utility/configuration consolidation belongs to #845.
The [frontend macro guide](../../tools/bazel/fe_app.md) is the source of truth for
the API, configuration contract and target commands.

## Consequences

Fewer application compilation actions reduce repeated startup and keep Vite and
Vitest close to their native application-wide workflow. A change invalidates a
larger compilation unit, trading fine-grained caching for lower action overhead.
This decision does not claim a measured speedup or change the repository-wide
package convention for other languages.

Separate production, test and story inputs keep test-only dependencies out of
production bundling. The shared TypeScript wrapper preserves coverage source maps
and original TS/TSX sources. Existing Mops commands, GraphQL generation and Vite
symlink workarounds remain compatible.
