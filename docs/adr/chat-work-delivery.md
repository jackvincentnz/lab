# Chat work delivery

## Status

Accepted.

Date: 2026-10-17
Decision owner: Jack Vincent

## Context

Saving a chat publishes domain events, and an `@Async` `@EventListener` in `ChatEventHandler`
answers pending assistant messages and runs approved tool calls. The client polls until the
assistant message leaves `PENDING`. Messages stayed `PENDING` forever in these cases:

- The listener threw, for example on a provider error or at the ten-completion limit, and
  nothing recorded the failure.
- The listener ran before the command's transaction committed and read a stale chat.
- Two listeners saved the same chat, and the loser failed the optimistic version check.
- A response mixed tools that need approval with tools that do not. The automatically approved
  calls never ran, so the turn never continued.

Mops runs on an in-memory H2 database, so a restart loses every chat. Work lost on a restart is
therefore not a defect today.

## Decision

Keep in-process delivery and make each failure visible instead of silent:

- Handlers are `@Async` `@TransactionalEventListener`, so they run after the saving transaction
  commits. `fallbackExecution` also delivers events published outside a transaction.
- A handler that throws, or a completion loop that reaches its limit, marks the assistant message
  `FAILED`. The existing UI shows the failure and its retry action.
- A handler reloads the chat after the model or tool call and applies its result only while the
  work is still current. A save that loses the version check reloads and reapplies the result,
  up to three times, without repeating the model or tool call.
- The chat queues automatically approved calls in a mixed response, and continues only when every
  tool call has a result or is rejected.

This accepts that work in flight is lost when the process stops. The message stays `PENDING`, and
an approved tool call stays without a result, so the persisted chat still shows the unfinished work.

## Consequences

- Handlers hold no transaction or database connection during model and tool calls, and commands
  never wait for a model call.
- A tool's own writes commit separately from its recorded result. When recording fails, the
  message is marked `FAILED`, and a retry can run the tool again.
- Handlers must check that their work is still current, because a command can cancel or replace a
  message while the model call runs.

## Alternatives considered

- **Transactional outbox with a scheduled worker.** Survives restarts and commits local tool
  effects with their result. It needs an outbox table, a recovery migration, row locks, and
  backoff state, and it holds a connection and the chat lock for a whole model call, so a user
  command waits for the model. The in-memory database removes the restart benefit.
- **Startup sweep for `PENDING` messages.** Resumes work after a restart from the state the chat
  already stores, but the in-memory database is empty at every start.

## Reconsider when

- Mops uses a persistent datasource: add a startup sweep that resumes `PENDING` messages and
  approved tool calls without results.
- A tool changes an external system: give it an idempotency key, or move tool work to an outbox.

## References

- [Spring transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html).
- [Spring Data JDBC optimistic locking](https://docs.spring.io/spring-data/relational/reference/jdbc/entity-persistence.html#jdbc.entity-persistence.optimistic-locking).
