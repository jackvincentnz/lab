# Agent guidance

Read [CONTRIBUTING.md](CONTRIBUTING.md) and the contribution guideline for each
area you change before you start:
[Bazel](docs/contributing/bazel.md), [Java](docs/contributing/java.md),
[testing](docs/contributing/testing.md),
[frontend](docs/contributing/frontend.md),
[dependencies](docs/contributing/dependencies.md), and
[docs](docs/contributing/readmes.md).

- This repository is public. Never commit employer or customer information,
  conversation transcripts, private links, real data, or database exports. Use
  synthetic examples.
- Answer a question without changing anything. Change only what was asked, and
  list adjacent work instead of doing it.
- Never merge a PR, close an issue, resolve review threads, or tick a tracking
  issue without explicit approval. Finishing an issue means an open PR with
  passing checks.
- Build the smallest change that meets the request. Propose any field, tool,
  guardrail, config, or dependency the request did not ask for, with its
  tradeoff, before adding it.
- Never remove behavior, tests, assertions, or lint rules to make a check pass.
  Fix the root cause with the mechanism upstream supports, or stop and ask.
- Validate before reporting a change as done, as described in
  [Validating a change](CONTRIBUTING.md#validating-a-change).
- Explain the root cause and the reason for each non-obvious change when you
  report back.
- Run Bazel with its default output base and flags. If a sandbox blocks a
  command, request approval for that exact command. If approval is refused, stop
  and report.
- Other agents and the owner run services on this machine. Stop only processes
  you started, by PID. Never use `pkill -f` or kill by port.
- Do not write outside the repository, for example to `~/.claude` or
  `~/.agents`, without asking. Put throwaway files in a temporary directory,
  not the repository.
- Keep this file to durable rules. Put conventions in `docs/contributing` and
  do not add task notes or history here.

Before creating or updating a GitHub issue, read and follow
[the GitHub issues skill](.agents/skills/github-issues/SKILL.md).
