# Day goals

## Purpose

A technical leader's work arrives from many places: morning planning, conversations during the
day, agent sessions, and an end-of-day reflection. It ranges from small individual tasks to
projects across several teams and long-running strategic problems. Daily notes capture it, but
notes are a poor place to answer one question: what matters most right now?

Day keeps every item of work in one list, in priority order, and makes changing that order fast.
It is a personal view. A team backlog tool such as Jira still owns the team's work.

## Goals

- See everything you are working on in one global priority order.
- Reprioritise in seconds, either by dragging an item in the app or by asking an agent to move it
  through [MCP](https://modelcontextprotocol.io), the protocol agents use to call tools.
- Keep the data in a local database rather than in notes, so that later features can query it.
- Start the window, the storage and the agent connection with one command.
- Stay small, and add a feature only when the daily process shows it is missing.

## An item

| Field   | Meaning                                         |
| ------- | ----------------------------------------------- |
| Name    | What the work is.                               |
| Status  | To do, doing, or done.                          |
| Rank    | Its place in the one global priority order.     |
| Created | When it was added.                              |
| Updated | When its name, status or rank was last changed. |

## Non-goals

- Replacing a team backlog, or syncing with one.
- Replacing daily notes.
- Several users, accounts, or sharing.
- Projects, tags, due dates, or progress tracking beyond status.

Access from another device is not a goal yet. The
[local service decision](../../../docs/adr/day-local-service.md) leaves room for it.
