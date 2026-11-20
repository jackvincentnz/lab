---
name: wide-refine
description: Refine a selected WIDE opportunity with the user, saving problems, candidate solutions, and source links. Use when the user asks to work through a triage recommendation, clarify impact, develop an approach, or refine an existing WIDE record. Does not implement or authorize delivery.
---

# WIDE refinement

Develop the selected opportunity into understood, comparable options. Stay within the user's scope.

1. Read strategy. Use `wide_search` to check for related work, then get relevant full records and sources.
   Previews aid discovery; they don't establish impact or absence of evidence. Read existing answers and
   human decisions before asking again. Check whether useful evidence or work already exists.
2. Identify the gap most likely to change a valuable decision. Ask one or two consequential questions.
   Rich supplied material can support a partial draft before asking; don't invent answers. Use the
   [problem guide](references/problem-template.md) or [candidate guide](references/solution-template.md)
   when helpful, rather than treating them as questionnaires.
3. Save concise current understanding in the relevant fields. Attribute claims and distinguish observed
   evidence, user judgment, and agent inference. Qualify conclusions from limited inspections; correct
   affected claims when evidence changes. Preserve meaningful context, uncertainty, and known sources.
   Include supporting notes and Markdown references in the existing content fields; no extra signal is needed.
4. Create drafts with known links, or edit selected fields while preserving unrelated content. Link
   candidates to needs with their expected contribution and limits; an unclear candidate may remain unlinked.
   Save questions individually. Answer, reopen, or move them with `wide_edit_question`; moving isn't answering.
   Record actual human choices as decisions. Agent recommendations and suggested reconsideration conditions
   stay labeled as proposals. An answer needn't also become a duplicate decision.
5. Explain what changed in understanding, cite IDs, and identify the most valuable remaining question or
   evidence need. Assess the bounded candidate; don't silently add adjacent work or infer readiness from
   populated fields. Preserve that assessment in the relevant problem or candidate content.

User-directed refinement authorizes faithful saves without per-write confirmation; discuss consequential
reinterpretations. ID receipts confirm successful writes. Read back when a relationship or interpretation
needs checking, rather than after every write; report partial saves honestly. Tool descriptions define
edit, clear, move, and link semantics.

Distinct new concerns may become signals for later consideration; a clear separate problem may be drafted
and linked directly. Don't silently broaden the current refinement. Do not implement, create delivery
work, or change durable strategy merely as part of refinement.
