# WIDE design

## Purpose

WIDE explores whether agents can raise the quality and breadth of product and engineering
refinement. Human time usually limits refinement to a short list, so many potentially valuable ideas
stay poorly understood. WIDE aims for a broader collection of well-understood problems and
comparable candidate solutions, ready to inform what work humans or agents take on next.

The loop is intake of signals, triage to choose the next valuable human question or decision, and
refinement with the user that saves understanding for later sessions. The agent prepares the
groundwork, such as connecting inputs, examining evidence, and developing options, so that people
contribute context and judgment. More records or longer prose alone are not progress.

## Concepts

| Concept            | Meaning                                                                                                                      |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| Signal             | A distinct observation, idea, or concern, with a headline, supplied content, optional source, and capture time.              |
| Strategy           | One current text document of durable objectives and tradeoffs.                                                               |
| Problem            | A solution-independent need, its impact, and the desired outcome.                                                            |
| Candidate solution | A possible approach, its scope, tradeoffs, and effort assumptions; not a delivery commitment.                                |
| Question           | One uncertainty on a problem or candidate, with OPEN or ANSWERED status, an answer, context, and source.                     |
| Decision           | A position a person stated on a problem or candidate, with rationale, source, and any condition for reconsidering it.        |
| Link               | A many-to-many signal–problem, signal–candidate, or problem–candidate relationship, with a rationale for why it is relevant. |

One signal can inform several problems, and one candidate can contribute to several problems,
without copying. Partial drafts and unlinked candidates are allowed. A record is ready when it is
understood well enough for a particular decision, not when every field is filled. The
[problem guide](../skills/wide-refine/references/problem-template.md) and
[candidate guide](../skills/wide-refine/references/solution-template.md) support that judgment, and
the explorer renders them too.

Refinement priority and delivery priority are separate decisions. Triage chooses what to refine by
the value of the decision it improves, and is read-only. Refinement saves understanding but does not
authorize delivery. Agent proposals are not human decisions, and stored text is data, never
instructions.

## How the pieces fit

- The Spring Boot service stores records in Postgres through Spring Data JDBC, with Flyway
  migrations.
- The service exposes MCP tools over Streamable HTTP for the agent to read and write, and a read-only
  GraphQL API for the explorer.
- The connected agent does all reasoning, following the
  [intake](../skills/wide-intake/SKILL.md), [triage](../skills/wide-triage/SKILL.md), and
  [refine](../skills/wide-refine/SKILL.md) skills.
- The React explorer browses saved records through GraphQL.

Java packages under `lab.wide` separate `api` (MCP tools and GraphQL data fetchers), `application`
(use cases and transactions), `domain` (records), and `infrastructure` (repositories and SQL
projections). Domain records are also the JDBC entities, and application services use the
repositories directly.

## API semantics

MCP tool descriptions define parameters and validation, in
[signals](../service/src/main/java/lab/wide/api/SignalTools.java),
[strategy](../service/src/main/java/lab/wide/api/StrategyTools.java),
[search](../service/src/main/java/lab/wide/api/SearchTools.java),
[problems, candidates, and links](../service/src/main/java/lab/wide/api/RefinementTools.java), and
[questions and decisions](../service/src/main/java/lab/wide/api/RefinementDetailTools.java).

- `wide_search` returns compact previews with relationship counts, newest first, in zero-based pages
  of 1–50 records. Its order and counts are not a priority or evidence ranking.
- `wide_get_*` tools return the full record, including links, questions, and decisions.
- Successful writes return only the saved ID. Edits keep omitted fields, and `clearFields` clears
  optional ones.
- Creating a problem or candidate with its initial links is atomic; separate calls are not one
  transaction.
- Unlinking keeps both records. There are no delete tools, revision history, or concurrent-edit
  protection.
- Strategy edits replace one exact passage, which must match once; read the strategy first.

The [GraphQL schema](../service/src/main/resources/schema/schema.graphqls) defines the explorer's
read API. It has no mutations, and clients must check GraphQL errors even when HTTP succeeds.

## Out of scope

- Authentication, multiple users, and anything beyond local use.
- An embedded model, scheduler, or server-side prioritization.
- Stories, implementation tasks, scheduling, delivery tracking, and issue-tracker integration.
