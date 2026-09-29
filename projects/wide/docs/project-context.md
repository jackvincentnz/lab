# WIDE project context

## Purpose

WIDE explores whether agents can increase productivity and improve the quality **and breadth** of
product and engineering refinement. Human time usually limits refinement to an early shortlist,
leaving many potentially valuable ideas poorly understood. The desired output is a broader collection
of well-understood problems and credible, comparable candidate solutions, ready to inform what work
humans or agents take on as capacity becomes available.

The loop is signal intake, triage to identify the next valuable human action or decision, and
collaborative refinement that saves understanding for future sessions. Agents should do useful
preparation—connect related inputs, examine evidence, develop options, and expose consequential gaps—
so humans can contribute context and judgment. More records, longer prose, and filled templates alone
do not demonstrate progress.

## Scope and prioritization

This is a local, single-user experiment sampling a few engineering initiatives where the user has
substantial context. Missing other product areas is intentional, not an intake gap or permanent
filter. Consider them when signals arrive. Multi-owner workflows are a future possibility.

**Refinement priority and delivery priority are different decisions.** Choose refinement by the value
of the decision it improves. Poorly understood opportunities can merit attention ahead of well-described
small improvements. Cheap checks, repeated mentions, and routine-work labels do not establish impact.
Strategy supplies objectives and tradeoffs; evidence, uncertainty, and human judgment shape the choice.

Delivery comparisons must consider both the problem and candidate contributions, scope, effort,
dependencies, feasibility, and capacity. A solution may address only part of a problem. The intended
future approach combines transparent calculations with owner-controlled ordering; recalculation must
not silently override human choices. No scoring formula, weights, or ordering model has been selected.
Unknown inputs must not become invented numbers or automatic zeros.

For now, focus on clear problems and candidate options. Stories, implementation tasks, scheduling,
execution tracking, and issue-tracker integration are outside scope. Do not split problems merely to represent
team deliverables: problems can span teams and candidates can contribute to several problems. There is
no required one-solution-per-team hierarchy.

## Domain decisions

| Concept            | Meaning                                                                                                                                                        |
| ------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Signal             | A distinct observation, idea, or concern, with a headline, supplied content, optional source, and capture time. Original content remains intact.               |
| Strategy           | One current text document of durable objectives and tradeoffs. Temporary conversational focus need not be saved.                                               |
| Problem            | A solution-independent need, its impact, and desired outcome.                                                                                                  |
| Candidate solution | A possible approach, scope, tradeoffs, and effort assumptions; not a delivery commitment.                                                                      |
| Question           | An individual uncertainty on a problem or candidate, with OPEN/ANSWERED status, an answer, context, and source.                                                |
| Decision           | An explicitly stated human position on a problem or candidate, with rationale, source, and any stated reconsideration condition.                               |
| Link               | A many-to-many signal–problem, signal–candidate, or problem–candidate relationship, with a rationale explaining relevance or expected contribution and limits. |

Clarifications and reasoning about known work belong on that work. Distinct concerns discovered in
conversation may become new signals; an understood separate problem may be drafted directly. One
signal can inform multiple problems without being copied. Use familiar language such as **Source**,
**Saved content**, **Questions**, and **Decisions**; signals have no classification field. Supporting
notes and references belong in existing content fields.

Preserve the distinction between observations, user judgments, and agent hypotheses. Derived analysis
is not independent corroboration. Estimates need a basis and uncertainty; separate active effort from
elapsed delay and retain sample scope before extrapolating. Correct current reasoning and link
rationales as evidence changes while preserving original source content. Agent proposals are not human
decisions, and proposed outcomes are not agreed targets.

Partial drafts and unlinked candidates are allowed. Do not invent evidence or a problem to complete a
record. Readiness depends on enough understanding for a particular decision, not field completeness
or an empty question list. The shared [problem](../skills/wide-refine/references/problem-template.md)
and [candidate](../skills/wide-refine/references/solution-template.md) guides support that judgment;
they are not mandatory questionnaires. Preserve settled answers, owner choices, and reasons for parking
work so future refinement builds on them.

## Architecture and deliberate limits

Spring Boot serves MCP over Streamable HTTP and a read-only GraphQL adapter over the same services.
Spring Data JDBC and Postgres persist the records, Flyway manages migrations, and Bazel builds the
project. The React/Mantine explorer uses Apollo and Vite. Reasoning runs in the connected agent;
workflow instructions live in the [skills](../skills), not in the server.

Java packages under `lab.wide` separate `api` (MCP/GraphQL adapters), `application` (use cases and
transactions), `domain` (entities and value types), and `infrastructure` (Spring Data repositories and
SQL projections). Domain records remain the JDBC entities; no separate persistence model is needed.
Application services use the repositories directly. Each layer has a Bazel target; API-specific input
annotations stay in `api`, which also maps domain records onto the DGS types generated from the
GraphQL schema. The Spring Boot entry point remains in `lab.wide` for package scanning.

- Use default Spring Data table mapping and standard repositories. Explicit SQL supports reads such
  as cross-entity search. IDs are generated UUIDs; stored text has no application-defined length caps.
- Compact search derives previews from existing fields instead of introducing summaries that can drift.
  Get tools retain full context. Compact write receipts avoid repeatedly loading unchanged records.
  Search order and relationship counts are not priority or evidence-strength assessments.
- Questions are individually editable and movable between problems and candidates. Earlier question
  blobs were preserved in main content under “Earlier refinement context”; that prose may still contain
  unresolved issues. There is one current question model, not a parallel legacy tool surface.
- Writes preserve identity and relationships; unlinking leaves endpoint records intact. There are no
  entity-delete tools, revision history, or concurrent-edit protection. Separate calls do not form an
  atomic refinement session, and paged reads do not provide a consistency snapshot.
- The current app and service are local and unauthenticated. There is no embedded model, scheduler,
  server-side prioritization, review metadata, or automatic readiness/lifecycle workflow.

Setup and verification commands belong in the [README](../README.md). Tool schemas own parameter
and write semantics; skills own the agent workflow; guides provide field-level help.

## API semantics

MCP tool descriptions define parameters and validation. The implementation is grouped into
[signals](../service/src/main/java/lab/wide/api/SignalTools.java),
[strategy](../service/src/main/java/lab/wide/api/StrategyTools.java),
[search](../service/src/main/java/lab/wide/api/SearchTools.java),
[problems, candidates, and links](../service/src/main/java/lab/wide/api/RefinementTools.java), and
[questions and decisions](../service/src/main/java/lab/wide/api/RefinementDetailTools.java).

- `wide_search` browses or searches all three entity types; `problemId` narrows to linked candidates.
  It returns full titles, field excerpts with `truncated` flags, relationship counts, and `hasMore`.
  Pages are zero-based, with a limit of 1–50 (default 20), newest first. Literal case-insensitive search
  covers entity text, excluding question/decision bodies and link rationales. Use `wide_get_*` for
  full records; problem/candidate reads include links, linked titles, questions, and decisions.
- Successful writes return only `{"id":"..."}`. Problem/candidate creation and initial links are atomic;
  separate calls are not one transaction. Edits preserve omitted fields; `clearFields` clears optional
  fields. Question edits can also move a question between parents without recreating it.
- Strategy has separate read, whole-document replace, and exact-passage edit tools. Read before editing;
  the selected passage must match once. Unset strategy has null content and update time.

The [GraphQL schema](../service/src/main/resources/schema/schema.graphqls) defines the explorer's read API
and relationships. Lists use zero-based pages and limits of 1–100 (default 50). There are no mutations;
clients must inspect GraphQL errors even when HTTP succeeds.

## Current checkpoint

Recent refinement exposed excessive retrieval context, awkward question moves after splitting a
problem, and competing representations of questions. Compact search, ID-only write receipts, movable
individual questions, and shorter skills address those issues. Triage should inspect material full
records before consequential comparisons and keep unsupported costs or predicted outcomes provisional.
Older unresolved work must remain eligible even without new signals; omitted preview text is not
absence of evidence.

The next step is feedback from real triage/refinement sessions with current tools and skills. Evaluate:

- Whether recommendations surface valuable human questions or decisions with useful preparation done.
- Whether understanding improves across more opportunities without proportionally more human effort.
- Whether candidate options become comparable while uncertainty and owner judgment remain visible.
- How much context is retrieved and how much user steering or repeated discovery is needed.

Unit tests cover service and adapter behaviour; a small set of Postgres-backed tests covers SQL,
migrations, and the MCP and GraphQL wire protocols. These verify mechanics, not recommendation quality. An assessment in an existing
development conversation is useful feedback but is not an independent test in a fresh agent session.
Numerical success targets have not been agreed.

Reflect on this checkpoint before adding model concepts. Review dates, refinement stages, calculated
priorities with manual ordering, and recurring automation remain deferred. Add richer evidence records,
import origin timestamps, or delivery handoffs only when the experiment demonstrates a need.
