---
name: pr-review-followup
description: Use when asked to follow up on, address, or work through review feedback on an open pull request.
---

# PR review follow-up

## Gather

Collect all feedback on the PR before you change code. Set `PR` to the PR
number.

- Reviews: `gh api repos/jackvincentnz/lab/pulls/$PR/reviews`. Review bodies
  can hold comments too, such as CodeRabbit nitpicks.
- Inline threads:

  ```sh
  gh api graphql -F owner=jackvincentnz -F repo=lab -F pr=$PR -f query='
    query($owner: String!, $repo: String!, $pr: Int!) {
      repository(owner: $owner, name: $repo) {
        pullRequest(number: $pr) {
          reviewThreads(first: 100) {
            nodes {
              id isResolved isOutdated path line
              comments(first: 50) { nodes { databaseId author { login } body } }
            }
          }
        }
      }
    }'
  ```

- PR comments, such as the CodeRabbit summary and the Codecov report:
  `gh api repos/jackvincentnz/lab/issues/$PR/comments`.
- CodeQL alerts:
  `gh api "repos/jackvincentnz/lab/code-scanning/alerts?ref=refs/pull/$PR/merge&state=open"`.
- Checks: `gh pr checks $PR`.

## Plan

Classify each unresolved item as fix, skip with a reason, or needs a decision
with the options. Check each bot finding against the current code first,
because bots review the diff without the repository's context, and comments
from before the latest push may be outdated. List a request that grows the
scope as a follow-up for the user, not as a fix. A CodeQL alert whose instances are all in test code is for
the user to dismiss. Report it, and do not change the code to avoid it.

Present the plan and wait for the user's go-ahead.

## Apply

1. Make the approved fixes,
   [validate](../../../CONTRIBUTING.md#validating-a-change) them, then commit
   and push.
2. Reply to each thread you fixed or skipped, including every bot thread
   marked Major or higher, with what changed or why it was skipped, so that a
   skip is a recorded decision:
   `gh api repos/jackvincentnz/lab/pulls/$PR/comments/<databaseId>/replies -f body='...'`,
   using the first comment's `databaseId`. Check that the response holds the
   reply's URL before you move on.
3. When the user asks you to resolve threads, resolve only the ones you fixed:

   ```sh
   gh api graphql -F id=<thread id> -f query='
     mutation($id: ID!) {
       resolveReviewThread(input: { threadId: $id }) { thread { isResolved } }
     }'
   ```

Do not resolve threads or merge unless the user asks.
