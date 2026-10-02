import type { TypedDocumentNode } from "@apollo/client";
import {
  ProblemsDocument,
  SignalsDocument,
  SolutionsDocument,
} from "../__generated__/graphql";

export type Kind = "signals" | "problems" | "solutions";

export const kinds = ["signals", "problems", "solutions"] as const;

export const labels: Record<Kind, string> = {
  signals: "Signals",
  problems: "Problems",
  solutions: "Candidates",
};

export const singular: Record<Kind, string> = {
  signals: "Signal",
  problems: "Problem",
  solutions: "Candidate",
};

/** The row shape shared by the three collection queries. */
export interface Entry {
  id: string;
  title: string;
  capturedAt?: string;
  updatedAt?: string;
  source?: string | null;
  signals?: { id: string }[];
  problems?: { id: string }[];
  solutions?: { id: string }[];
}

export interface ListVariables {
  page: number;
  limit: number;
  query?: string;
}

export const listQueries: Record<
  Kind,
  TypedDocumentNode<Partial<Record<Kind, Entry[]>>, ListVariables>
> = {
  signals: SignalsDocument,
  problems: ProblemsDocument,
  solutions: SolutionsDocument,
};

export const pageSize = 25;
