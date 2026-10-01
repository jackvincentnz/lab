- Follow [the GitHub issues skill](.agents/skills/github-issues/SKILL.md) when
  creating or updating a GitHub issue, so that issues fit how the user tracks
  work.
- Keep employer and customer information, transcripts, private links, real
  data, and database exports out of this public repository, so that nothing
  private is published.
- Merge a PR, resolve a review thread, create or close an issue, or edit a
  tracking issue only when the user explicitly asks, so that the user decides
  what lands and what is planned. An issue is finished when its PR is open with
  passing checks.
- When the user asks a question, answer it without changing anything, so that
  the user decides what happens next.
- Propose any field, tool, guardrail, or configuration the request did not ask
  for, with its tradeoff, before adding it, so that the change stays within the
  request.
- Fix the cause of a failing check without removing behavior, tests,
  assertions, or lint rules, so that a passing check still means the code
  works.
- Ask before stopping a process you did not start, and stop processes by PID
  rather than with `pkill -f` or by port, so that the user's other servers and
  sessions keep running.
- Ask before writing outside the repository, such as to `~/.agents` or
  `~/.claude`, so that every change stays visible in review.
- Run Bazel with its default output base and flags, so that it reuses the
  checkout's cache.
- When a sandbox blocks a command, request escalation for that exact command
  and stop if it is refused, so that the user approves each access instead of
  a workaround.
- Report the root cause and the reason for each non-obvious change, so that
  the user can review the change without asking why.
