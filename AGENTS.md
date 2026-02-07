# AGENTS

Use this file to capture working notes, conventions, and repo-specific gotchas. Keep entries short and date-stamped so future edits are easy to trust.

## Maintenance rule

- Update this file whenever new information is learned or a change makes any entry outdated.
- Keep formatting Prettier-friendly: blank line after headings and before lists.

## Repo overview

- Monorepo using Bazel (see `MODULE.bazel`, `WORKSPACE`).
- Primary stacks: Java (Spring Boot 3) backends and Vite + React + TypeScript frontends.
- Node/pnpm versions: Node >=18.20.4, pnpm >=8.6.7 <9 (from `package.json`).
- pnpm workspace packages are listed in `pnpm-workspace.yaml`.

## Layout map

- `projects/`: applications and services.
- `libs/`: shared libraries (frontend and backend).
- `tools/bazel/`: Bazel macros and helper scripts.
- `docs/`: repo conventions (style, tooling).
- `infra/`: infrastructure-related code and configs.

## Common commands

- `bazel build //...`
- `bazel test //...`
- Default pattern for package targets: `bazel <command> //<PATH_TO_PROJECT>:<TARGETS>`.

## Conventions

- Commit messages follow Conventional Commits (see `docs/style.md`).
- Pre-commit, Prettier, and ESLint enforce formatting and linting.

## Quick reminders

- Frontend apps typically live under `projects/*/*` with `vite.config.ts`, `index.html`, and `src/`.
- Java services usually follow `src/main/java`, `src/test/java`, `src/main/resources`, and `src/main/proto` with Bazel `BUILD.bazel` files.

## Open questions / follow-ups

- None currently.

## Agent notes

- 2026-02-07: Initial AGENTS.md created with repo structure and common commands.
- 2026-02-07: Updated `pnpm-workspace.yaml` to use `projects/mops/app`.
- 2026-02-07: Replaced organizer-specific Bazel examples with the `//<PATH_TO_PROJECT>:<TARGETS>` pattern.
- 2026-02-07: Removed pnpm command wrappers in favor of Bazel-only build/test/run usage.
- 2026-02-07: Added formatting guidance to reduce Prettier-only diffs.
- 2026-02-07: Moved organizer local-environment targets and docs into `projects/organizer/`.
- 2026-02-07: Granted organizer package visibility to shared deliver/load targets for local environment runs.
- 2026-02-07: Set organizer e2e and task test suite timeouts to `short` to match observed runtimes.
- 2026-02-07: Removed root `//:__pkg__` visibility from organizer-local targets after hoisting.
- 2026-02-07: Added `projects/mops/README.md` with Mops quickstart commands and agent-focused project map.
