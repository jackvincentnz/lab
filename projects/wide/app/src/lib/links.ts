import type { FragmentType } from "../__generated__";
import type {
  CandidateFieldsFragmentDoc,
  ProblemFieldsFragmentDoc,
  SourceFieldsFragmentDoc,
} from "../__generated__/graphql";

/** A saved relationship; each extension adds the record on its far side. */
export interface Link {
  id: string;
  rationale: string;
  linkedAt: string;
}

export interface SourceLink extends Link {
  signal: FragmentType<typeof SourceFieldsFragmentDoc>;
}

export interface ProblemLink extends Link {
  problem: FragmentType<typeof ProblemFieldsFragmentDoc>;
}

export interface SolutionLink extends Link {
  solution: FragmentType<typeof CandidateFieldsFragmentDoc>;
}
