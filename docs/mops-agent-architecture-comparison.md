# Mops Agent Architecture: Pattern Comparison with OpenAI Codex

## Purpose

This document compares **architectural patterns** in Mops and the open-source Codex repository, then recommends which patterns better support Mops goals.

> Scope note: this focuses on orchestration, state, approval, and reliability patterns rather than language/runtime/toolchain differences.

## Mops goals inferred from repo context

From `projects/mops/eval/questions.md`, Mops appears optimized for:

- Reliable budget/forecast retrieval and reconciliation workflows.
- Safe tool use over domain data (including approval for potentially consequential actions).
- Multi-step analytical answers with clear business outputs.
- Deterministic behavior suitable for evaluation and iteration.

## Pattern comparison

### 1) Conversation + execution state model

**Mops pattern**

- Domain aggregate (`Chat`) stores the conversation timeline, tool-call status, and transitions via domain events.
- Message lifecycle is explicit: pending -> completed/cancelled; tool calls move through pending approval / approved / rejected.
- Assistant turns are persisted first, then completed asynchronously by event handlers.

**Codex pattern**

- Thread-centric runtime where each thread is a conduit for operations/events and richer session metadata/config snapshots.
- Turn state, submission IDs, rollout history, event streams, and policy settings are first-class and continuously emitted.
- The thread manager centralizes lifecycle concerns (spawn, fork, shutdown, history truncation, rollout handling).

**Core architectural difference**

- Mops treats chat state primarily as a **domain aggregate** with event-driven side effects.
- Codex treats interaction as a **typed runtime protocol stream** with strong lifecycle/event semantics.

---

### 2) Orchestration loop and control boundaries

**Mops pattern**

- A single event handler runs the loop: model response -> tool execution (if no approval needed) -> follow-up completion.
- Loop has a fixed max iteration guard (`MAX_COMPLETIONS`).
- Approval gates happen when tool metadata says approval is required.

**Codex pattern**

- Orchestration is decomposed: session/thread manager + execution policy + explicit MCP tool-call handling.
- Tool-call handling includes begin/end events, metadata lookup, policy routing, optional approval workflows, and telemetry around each call.
- Approval routing can include dedicated review pathways (e.g., guardian review), not only direct user decisions.

**Core architectural difference**

- Mops concentrates control in one coordinator.
- Codex distributes control into **specialized orchestration modules** with narrower responsibilities.

---

### 3) Approval and safety model

**Mops pattern**

- Approval is a per-tool policy (`needsApproval`) resolved from environment-driven config.
- Human-in-the-loop is modeled in-domain through `PENDING_APPROVAL` and subsequent approve/reject commands.
- Simpler and understandable, but mostly binary and synchronous from an architectural perspective.

**Codex pattern**

- Multi-layered policy model: command/mcp policy evaluation, rule-based approvals, sandbox/network policy checks, and optional delegated review.
- Approvals are treated as policy decisions attached to call lifecycle and telemetry.
- “Fail-closed” style patterns are visible in approval-related flows.

**Core architectural difference**

- Mops: **tool-level approval flags**.
- Codex: **policy engine + lifecycle-aware approval pipeline**.

---

### 4) Reliability and failure handling

**Mops pattern**

- Clear known risk is documented in code (`FIXME`: async event listener may leave assistant message pending).
- Exceptions in orchestration can surface abruptly (`RuntimeException` at max completions).
- Strong domain transitions exist, but recovery/idempotency strategy is implicit.

**Codex pattern**

- Extensive explicit lifecycle controls (thread shutdown reporting, fork semantics, event typing, status updates).
- More hooks for replay, inspection, and recovery due to evented/threaded runtime.
- Heavy emphasis on durable state snapshots and runtime observability.

**Core architectural difference**

- Mops prioritizes lean orchestration and domain clarity.
- Codex prioritizes **operational resilience and observability at runtime scale**.

---

### 5) Extensibility model

**Mops pattern**

- Tools are interface-based and provider-backed; model adapter and tool adapter are isolated behind application interfaces.
- Good foundation for bounded-domain assistants.
- Extension pressure likely appears when adding nuanced approval modes, retries, or multi-agent workflows.

**Codex pattern**

- Extensibility is protocol-first: thread operations, config layers, skills/plugins, MCP resources/tools, and policy overlays.
- Better for heterogeneous, evolving multi-surface agent ecosystems.

**Core architectural difference**

- Mops is **domain-first extensibility**.
- Codex is **platform/runtime extensibility**.

## Recommendations for Mops goals

### Near-term (best fit now)

1. Keep the Mops **domain aggregate + event model** as the source of truth for chat/business state.
2. Introduce a small **turn state machine** at application layer (queued/running/waiting_approval/completed/failed/timed_out) separate from domain message status.
3. Replace “throw on max completions” with typed failure outcomes persisted on the turn (recoverable vs terminal).
4. Add idempotency keys for tool executions and approval resolutions to avoid duplicate side effects.
5. Add execution tracing per turn (tool call begin/end + latency + decision reason), even before adopting a larger runtime architecture.

Why this aligns with Mops goals:

- Preserves domain correctness and auditability for financial workflows.
- Reduces “stuck pending” and opaque failures.
- Improves evaluation quality by making failures classifiable and repeatable.

### Mid-term (borrow from Codex patterns selectively)

1. Introduce a **policy decision service** decoupled from tool definitions (instead of only `needsApproval` flags).
2. Split orchestration into components:
   - turn coordinator,
   - tool execution dispatcher,
   - approval workflow service,
   - history/context builder.
3. Add a replayable event stream for operations (not just domain events) to support diagnostics and evaluation replay.
4. Allow configurable approval modes per action category (read-only, analytical, mutating, external side effect).

This brings Codex-like robustness while avoiding over-platforming early.

## Which architecture aligns better with Mops?

- **For current product goals (finance planning assistant in a bounded domain):** Mops’s current domain-centric pattern is the better base.
- **For future goals (broader automation, many tools, many channels, high-concurrency operations):** Codex-style runtime modularization is better.

Practical recommendation:

- Do **not** wholesale copy Codex architecture.
- Evolve Mops toward a **hybrid**: keep domain aggregate boundaries, adopt Codex-inspired lifecycle/policy/observability patterns where reliability gaps are most visible.

## Alternative architectures neither side fully uses

### A) Workflow/DAG turn engine

- Represent each assistant turn as a DAG: context build -> plan -> tool batch -> synthesis -> validation -> finalize.
- Benefits: retry at node level, explicit checkpoints, partial recompute.
- Tradeoff: more orchestration overhead.

### B) Intent-to-plan compiler with policy-aware plan verifier

- First phase compiles user intent into a typed plan.
- Second phase verifies policy/safety/approval requirements before execution.
- Third phase executes only verified plan steps.
- Benefits: stronger predictability and auditability for regulated or financial contexts.

### C) Event-sourced operation log + deterministic reducer

- Persist every operation as immutable events and derive state through a reducer.
- Benefits: perfect replay for eval/regression, root-cause analysis, and scenario testing.
- Tradeoff: migration and storage/complexity costs.

### D) Two-agent architecture (planner + executor) with strict contract

- Planner can propose actions; executor can only run contract-compliant, policy-approved steps.
- Benefits: reduces accidental unsafe calls; easier to benchmark planning quality separately from execution quality.

## Suggested phased adoption roadmap

1. **Phase 1:** Add turn-state machine + failure taxonomy + idempotent tool resolution.
2. **Phase 2:** Introduce policy decision service and richer approval modes.
3. **Phase 3:** Add operational event log + replay harness integrated with `projects/mops/eval`.
4. **Phase 4:** Evaluate planner/executor split for strategic analysis prompts.

This sequence gives immediate reliability benefits while preserving velocity.
