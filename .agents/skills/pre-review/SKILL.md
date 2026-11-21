---
name: pre-review
description: Use before reporting a PR ready, or when asked to review a branch, so that a fresh reader checks the change against the request and the repository's conventions.
---

# Pre-review

## Gate

Set `BASE=$(git merge-base origin/main HEAD)` and
`HASH=$(git diff $BASE | git hash-object --stdin)`. Skip the review, and say
so, when the diff is empty, holds only generated files such as lockfiles, or
has the same `HASH` as the last entry for this branch in the
[log](#record), so that a rebase or an empty push does not start another
review.

## Review

Start two reviewer subagents in parallel, each with a fresh context and
read-only tools. Give each the user's request or the issue text, `BASE`, and
`git diff $BASE`, but not your own summary or conclusions, so that your
reasoning does not steer them. Without subagents, do both passes yourself from
the request and the diff alone.

- The standards reviewer reads [AGENTS.md](../../../AGENTS.md),
  [CONTRIBUTING.md](../../../CONTRIBUTING.md), and the guides in
  `docs/contributing/` that cover the changed files, and checks that:
  - The change uses the documented mechanism, or the PR says why not.
  - Each added comment says why, and the code and names cannot say it.
  - Tests cover our behavior once, in the class that owns it.
  - Every branch is reachable.
  - Docs that describe the changed behavior are updated, plain, and concise.
  - The PR title states the behavior, and the body has only the template's
    sections, with absolute links.
  - No Gazelle-owned attribute was edited by hand and no hook was skipped.
- The intent reviewer compares the diff with the request, and checks for
  missing work, anything the request did not ask for, and claims in the PR
  body or docs that the code or the pinned version's docs do not support.

Each reviewer reports at most about 400 words, with these severities:

| Severity     | Meaning                                                                                      |
| ------------ | -------------------------------------------------------------------------------------------- |
| Important    | Wrong for an input this repository can produce, misses the request, or breaks a quoted rule. |
| Nit          | Worth changing but not blocking. Report at most five.                                        |
| Pre-existing | Wrong before this change. Report it, but do not fix it here.                                 |

Each finding gives a `file:line` and either the quoted rule or the input and
code path that fail. Reviewers do not report style that formatters own,
anything pre-commit or CI already checks, test coverage the guides do not ask
for, or risks without evidence, so that every finding is worth acting on.

## Verify

Give the Important findings to one more subagent with a fresh context and
read-only tools, which confirms or rejects each from the code. Act only on
confirmed findings, so that a reviewer's mistake does not become a change.

## Fix and repeat

Fix the confirmed Important findings, validate, and run the review again. Stop
after three rounds or when only nits remain, and report anything still open,
because running out of rounds is not approval.

## Ready check

Check each criterion under
[Handing off in AGENTS.md](../../../AGENTS.md#handing-off) yourself, and report
the PR as Waiting or Blocked when one is not met, so that Ready means
mergeable.

## Record

Append one line per round to the log outside the working tree, so that review
precision and escapes can be measured later:

```sh
LOG="$(git rev-parse --git-common-dir)/pre-review.jsonl"
```

Write one JSON object with `date`, `branch`, `pr`, `hash`, `lines` (added plus
removed), `round`, and `findings`, a list of objects with `reviewer`,
`severity`, `verified` (true or false), and `outcome` (`fixed`, `skipped`, or
`open`).

## Report

Tell the user how many rounds ran, what was fixed, what was skipped and why,
and the nits left for them to decide.
