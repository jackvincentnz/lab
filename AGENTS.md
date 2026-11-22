# Agent guidance

The [contributing guide](CONTRIBUTING.md) describes the output. These rules
describe how to work.

## Scope and decisions

- When the user asks a question, answer it without changing anything, so that
  the user decides what happens next.
- Before work that spans several steps or PRs, ask every question whose answer
  would change the approach in one numbered list, each with the default you
  will use, so that the user settles them in one reply. Look up facts in the
  code, docs, and tools instead of asking, and go ahead without more check-ins
  unless a new decision comes up.
- Make reversible choices yourself, following the surrounding code and docs,
  and list each one in your report, so that the user can reverse one cheaply.
- Propose any field, tool, guardrail, or configuration the request did not ask
  for, with its tradeoff, before adding it, so that the change stays within the
  request.
- Remove code, resets, or workarounds that your change makes redundant in the
  same PR, so that no dead workaround is left behind.
- Merge a PR, resolve a review thread, create or close an issue, or edit a
  tracking issue only when the user explicitly asks, so that the user decides
  what lands and what is planned. An issue is finished when its PR is open with
  passing checks.
- Follow [CONTRIBUTING.md](CONTRIBUTING.md) and the guides it links when a
  skill or other agent instruction differs from them, and propose a fix to the
  skill, so that each convention has one owner.
- Follow [the GitHub issues skill](.agents/skills/github-issues/SKILL.md) when
  creating or updating a GitHub issue, so that issues fit how the user tracks
  work.
- Keep employer and customer information, transcripts, private links, real
  data, and database exports out of this public repository, so that nothing
  private is published.

## Working

- Check a claim against the code, the docs for the pinned version, or a tool's
  output before you state it, say how you checked it, and name what you could
  not check, so that the user does not decide on a wrong premise.
- Agent shells do not load direnv, so run repository tools such as `aspect`
  and `pnpm` from the main checkout's `tools/.bazel_env/bin`, which
  [getting started](README.md#getting-started) sets up, so that you use the
  pinned versions without building them in every worktree.
- When your branch's tool versions differ from the main checkout's, such as
  after a change to `MODULE.bazel`, `tools/tools.lock.json`, or
  `packageManager` in `package.json`, run `bazel run //tools:bazel_env` in your
  worktree and validate with its own `tools/.bazel_env/bin`, so that
  validation uses your branch's versions.
- Run Bazel with its default output base and flags, so that it reuses the
  checkout's cache.
- Expect other sessions to run Bazel at the same time. When a test times out,
  rerun only that target after your other Bazel work finishes, before you
  report it failed, so that a slowdown from shared CPU is not reported as a
  regression.
- When the work in a worktree you created with `git worktree add` is done, run
  `bazel clean --expunge` in it and then remove it, so that neither the
  worktree nor its Bazel output base, which is outside the worktree, fills the
  disk.
- When you split work across sessions or subagents, run Bazel-heavy steps in
  one session at a time, so that parallel builds do not exhaust the disk.
- Stop, delete, or reset only the processes, branches, worktrees, and Docker
  volumes this session created, and ask about anything else, so that other
  sessions and local data survive. Stop a process by PID rather than with
  `pkill -f` or by port.
- Stage the files you changed by path, and keep personal agent files such as
  `.claude/launch.json` and local hooks out of commits unless asked, so that a
  commit holds only the change.
- Ask before writing outside the repository, such as to `~/.agents` or
  `~/.claude`, so that every change stays visible in review.
- When a sandbox blocks a command, request escalation for that exact command
  and stop if it is refused, so that the user approves each access instead of
  a workaround.
- Fix the cause of a failing check instead of removing behavior, tests,
  assertions, or lint rules to make it pass, so that a passing check still
  means the code works.
- Run CI experiments with `gh workflow run main.yml --ref <branch>` instead of
  opening PRs, so that experiments do not crowd the PR list.

## Handing off

- Before you report a PR ready, make sure that the branch is current with
  `main`, lockfiles are regenerated, CI has finished green on the latest push,
  every bot finding is fixed or answered with a reason, and your validation
  covers every change in the diff, so that the user does not ask for each.
- Gate a push on a check's exit code, not on filtered output, and say when
  results came from cache, so that a green report means the checks passed.
- Run the [pre-review skill](.agents/skills/pre-review/SKILL.md) before you
  report a PR ready, so that the user reviews work a second reader has already
  checked.
- Lead a report with the answer in plain words. For a PR, lead with Ready
  (mergeable now), Waiting (on CI or a reviewer), or Blocked (needs a fix or a
  decision). Define any term you introduce, give each rejected alternative
  one line, and make any command for the user complete, so that the user can
  decide without follow-up questions.
- Report the root cause and the reason for each non-obvious change, so that
  the user can review the change without asking why.
- Say in a PR's report how it will land, as one squash commit or as the list of
  commits a rebase merge will add, so that the user can review the history
  `main` will get.
- When asked to tidy a PR, rewrite its branch into one commit per change and
  force-push it, so that `main` gets one revertible commit per change.

## Enforcement

| Rule                                          | Enforced by                                      |
| --------------------------------------------- | ------------------------------------------------ |
| Conventional commit messages                  | Commitlint `commit-msg` hook                     |
| Formatting and lint                           | pre-commit hooks and CI                          |
| Java BUILD files match Gazelle                | Gazelle `pre-push` hook and CI diff check        |
| `MODULE.bazel.lock` is current                | `--lockfile_mode=error` in CI                    |
| Contributing conventions and handoff criteria | [Pre-review](.agents/skills/pre-review/SKILL.md) |
| Other rules in this file                      | Nothing yet                                      |

When the user corrects the same thing twice, propose the strongest enforcer
that can judge it, in this order: a check or test, then a rule in the
contributing guides, which pre-review checks, or here, so that a correction
does not recur.
