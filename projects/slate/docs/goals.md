# Slate goals

## Purpose

A technical leader's work arrives from many places: morning planning, conversations during the
day, agent sessions, and an end-of-day reflection. It ranges from small individual tasks to
projects across several teams and long-running strategic problems. Daily notes capture it, but
notes are a poor place to answer one question: what matters most right now?

Slate keeps every item of work in one list, in priority order, and makes changing that order fast.
It is a personal view. A team backlog tool such as Jira still owns the team's work.

## Goals

- See everything you are working on in one global priority order.
- Reprioritise in seconds, either by dragging an item in the app or by asking an agent to move it
  through [MCP](https://modelcontextprotocol.io), the protocol agents use to call tools.
- Do by hand in the app anything that an agent can do through MCP.
- Keep the data in a local database rather than in notes, so that later features can query it.
- Start the window, the storage and the agent connection with one command.
- Stay small, and add a feature only when the daily process shows it is missing.

## An item

| Field       | Meaning                                                                                                                                               |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| Name        | What the work is.                                                                                                                                     |
| Description | Context that belongs with it, such as notes, links or why it matters. Markdown.                                                                       |
| Sources     | Where it came from, the main one first, such as the agent session that captured it: https links or references such as a file path, each with a label. |
| Status      | To do, in progress, done, deferred, won't do, or delegated. The list leaves out the last three.                                                       |
| Rank        | Its place in the one global priority order.                                                                                                           |
| Created     | When it was added.                                                                                                                                    |
| Updated     | When it was last changed.                                                                                                                             |
| Done        | When it was marked done. The list shows a done item only on that day.                                                                                 |

## Non-goals

- Replacing a team backlog, or syncing with one.
- Replacing daily notes.
- Several users, accounts, or sharing.
- Projects, tags, due dates, or progress tracking beyond status.

Access from another device is not a goal yet. The
[local service decision](../../../docs/adr/slate-local-service.md) leaves room for it.
