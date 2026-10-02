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
with the options. A CodeQL alert whose instances are all in test code is for
the user to dismiss. Report it, and do not change the code to avoid it.

Present the plan and wait for the user's go-ahead.

## Apply

1. Make the approved fixes, [validate](../validate/SKILL.md) them, then commit
   and push.
2. Reply to each thread with what changed:
   `gh api repos/jackvincentnz/lab/pulls/$PR/comments/<databaseId>/replies -f body='...'`,
   using the first comment's `databaseId`.
3. Resolve only the threads that you fixed:

   ```sh
   gh api graphql -F id=<thread id> -f query='
     mutation($id: ID!) {
       resolveReviewThread(input: { threadId: $id }) { thread { isResolved } }
     }'
   ```

Do not resolve threads or merge before the user approves.
