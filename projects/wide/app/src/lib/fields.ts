import type {
  CandidateFieldsFragment,
  ProblemFieldsFragment,
} from "../__generated__/graphql";

// Field guidance mirrors the shared refinement guides shown in the drawer.
export interface FieldSpec<T> {
  key: keyof T;
  title: string;
  hint: string;
}

export const problemFields: FieldSpec<ProblemFieldsFragment>[] = [
  {
    key: "description",
    title: "Problem & evidence",
    hint: "What happens, who experiences it, and in what circumstances. Evidence with its origin date, limitations, and confidence.",
  },
  {
    key: "impact",
    title: "Impact",
    hint: "Frequency, affected population, active effort or elapsed delay, or risk. Distinguish measured baselines from estimates, and say why refinement matters now.",
  },
  {
    key: "desiredOutcome",
    title: "Desired outcome",
    hint: "The change that would matter to the affected people and how to recognize it. Distinguish proposed outcomes from outcomes agreed with the user.",
  },
];

export const candidateFields: FieldSpec<CandidateFieldsFragment>[] = [
  {
    key: "approach",
    title: "Approach",
    hint: "How the proposed change would produce the outcome. Separate the source's idea from additions made during refinement.",
  },
  {
    key: "scope",
    title: "Scope & acceptance",
    hint: "Included and excluded behavior, boundaries, and acceptance conditions tied to the problem's outcome. Label unagreed scope as proposed.",
  },
  {
    key: "tradeoffs",
    title: "Tradeoffs & dependencies",
    hint: "Alternatives, dependencies, risks, and reversibility. Say whether other candidates compete with, complement, or depend on this one.",
  },
  {
    key: "effort",
    title: "Effort & assumptions",
    hint: "An estimate or range with its basis and confidence. Unknown effort is valid; avoid false precision.",
  },
];
