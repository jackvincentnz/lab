---
name: wide-intake
description: Capture engineering observations, friction, architecture ideas, objectives, and feedback in WIDE, or retrieve saved signals. Use when the user asks to capture or retrieve WIDE signals.
---

# WIDE intake

- Capture with `wide_capture_signal`: a concise agent-written title, supplied content, and optional known source.
  Preserve wording, attribution, and uncertainty. Light formatting cleanup is fine; keep agent analysis out
  of the captured content. Don't demand evidence, priority, or a solution.
- A distinct concern discovered during refinement can be a signal. Clarifications, answers, and reasoning
  about known work update that work through refinement. Honor explicit capture requests; don't duplicate
  an observation merely because it informs several problems. Repeated observations may be captured separately.
- A successful write returns the generated ID. Confirm it with the title; report failures plainly.
- Use `wide_search` to browse or search signals and `wide_get_signal` for full saved content.
