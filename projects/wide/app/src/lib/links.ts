import type { FragmentType } from "../__generated__";
import type {
  CandidateFieldsFragmentDoc,
  ProblemFieldsFragmentDoc,
  SourceFieldsFragmentDoc,
} from "../__generated__/graphql";

/** A saved relationship together with the record on its far side. */
export type Link<T> = { id: string; rationale: string; linkedAt: string } & T;

export type SourceLink = Link<{
  signal: FragmentType<typeof SourceFieldsFragmentDoc>;
}>;
export type ProblemLink = Link<{
  problem: FragmentType<typeof ProblemFieldsFragmentDoc>;
}>;
export type SolutionLink = Link<{
  solution: FragmentType<typeof CandidateFieldsFragmentDoc>;
}>;
