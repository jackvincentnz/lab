# Durable chat delivery

Status: Accepted.

## Context

An asynchronous Spring event listener could lose work on a process restart or run before the
chat transaction committed. Saved assistant messages then stayed `PENDING` while the client
polled. Completion loops also threw at their limit without recording a user-visible failure.

## Decision

Chat commands run in a transaction. A Spring Data after-save callback requires the repository save transaction
and inserts completion or approved-tool work into `CHAT_OUTBOX`. Unlike repository domain-event publication,
the callback runs inside the save transaction, including for direct repository saves. A migration recovers existing
pending assistant messages and approved tools without results. The existing client polling stays.

A scheduled worker claims a chat and one work item with `FOR UPDATE SKIP LOCKED`. Chat commands
lock the same chat before reading it. These locks serialize sibling tools and commands, while
other workers can process other chats. The worker commits the message, local database tool
effects, follow-up work, and acknowledgement together. A failed attempt rolls back completely,
then a fresh transaction locks the surviving work to record retry metadata. A repository failure
can mark its whole transaction rollback-only, so a savepoint would not reliably retain that
metadata. If another worker finishes in the gap, the retry sees no work and does nothing.
Durable retries use backoff and stop after five attempts.
A process crash rolls back the transaction and releases the locks, leaving work available again.
Restart durability requires a persistent configured database; the default in-memory H2 demo
loses the entire database on restart. Use the existing Spring datasource settings to select persistent H2 storage.

Work is unique by chat, message, and tool call. Delivery checks the current message and tool
state, so cancelled, removed, completed, rejected, or already-executed work becomes a no-op.
Automatically approved calls in a mixed tool response are queued too. A continuation is created
only when every tool has a result or is rejected. User approval remains required for other tools.
The existing ten-completion bound and exhausted delivery retries persist `FAILED` with a safe
reason, which the API returns and the UI displays with its existing retry action.

## Consequences

Transactions and connections stay open during model and tool calls. A command against the same
chat waits for its worker, trading responsiveness and database capacity for simple atomic local
tool effects and restart recovery. The scheduler processes bounded batches every second.
Model calls may repeat after rollback and incur extra provider cost. Tools currently change the
same database and participate in the transaction. Future tools with external side effects must
supply their own idempotency key or reconciliation: the outbox alone cannot make those effects
exactly once. An after-commit listener plus reconciliation was rejected because delivery still
needs durable work and coordination for approved tools.

## Validation

Database integration tests exercise atomic enqueue and rollback, restart delivery, duplicate
work, concurrent claims, backoff, retry exhaustion, and rollback of tool effects. Domain and
handler tests check completion bounds, stale messages, duplicate tool delivery, and waiting for
all tool results. UI tests check that the persisted failure reason is displayed and retry works.
