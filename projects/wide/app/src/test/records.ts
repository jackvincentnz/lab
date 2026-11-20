import type {
  CandidateFieldsFragment,
  DecisionFieldsFragment,
  ProblemFieldsFragment,
  QuestionFieldsFragment,
  SourceFieldsFragment,
} from "../__generated__/graphql";

// Synthetic records shaped like the WIDE fragments; Apollo needs each `__typename` to read fragments back.

export const time = "2026-01-01T00:00:00Z";

export function signalFields(overrides: Partial<SourceFieldsFragment> = {}) {
  return {
    __typename: "Signal" as const,
    id: "signal-1",
    title: "Synthetic observation",
    content: "  Verbatim\nwith whitespace  ",
    source: "Synthetic fixture",
    capturedAt: time,
    ...overrides,
  };
}

export function problemFields(overrides: Partial<ProblemFieldsFragment> = {}) {
  return {
    __typename: "Problem" as const,
    id: "problem-1",
    title: "Finding the demo guide",
    description: "Visitors have trouble finding help",
    impact: "Unknown frequency",
    desiredOutcome: "Proposed: make help easy to find",
    updatedAt: time,
    ...overrides,
  };
}

export function candidateFields(
  overrides: Partial<CandidateFieldsFragment> = {},
) {
  return {
    __typename: "Solution" as const,
    id: "solution-1",
    title: "Contextual guide",
    approach: "Proposed: offer help nearby",
    scope: "Only the landing page",
    tradeoffs: "May complement navigation changes",
    effort: "A few days",
    updatedAt: time,
    ...overrides,
  };
}

export function questionFields(
  overrides: Partial<QuestionFieldsFragment> = {},
) {
  return {
    __typename: "RefinementQuestion" as const,
    id: "question-1",
    question: "How often does this happen?",
    status: "OPEN" as const,
    answer: null,
    context: null,
    source: null,
    updatedAt: time,
    ...overrides,
  };
}

export function decisionFields(
  overrides: Partial<DecisionFieldsFragment> = {},
) {
  return {
    __typename: "RefinementDecision" as const,
    id: "decision-1",
    decision: "Focus on finding existing help",
    rationale: null,
    source: null,
    revisitWhen: null,
    updatedAt: time,
    ...overrides,
  };
}

export function link<T extends object>(record: T, rationale: string) {
  return { id: `link-${rationale}`, rationale, linkedAt: time, ...record };
}
