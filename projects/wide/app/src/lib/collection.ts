import type { Entry, Kind } from "./kinds";

export type Filter = "all" | "unlinked" | "linked";

export function parseFilter(value: string | null): Filter {
  return value === "linked" || value === "unlinked" ? value : "all";
}

export function isLinked(kind: Kind, entry: Entry) {
  if (kind === "solutions") return (entry.problems?.length || 0) > 0;
  return (
    (entry.signals?.length || 0) +
      (entry.problems?.length || 0) +
      (entry.solutions?.length || 0) >
    0
  );
}

const unlinkedPhrase: Record<Kind, string> = {
  signals: "not yet linked to a problem or candidate",
  problems: "not yet linked to a signal or candidate",
  solutions: "without a linked problem",
};
const linkedPhrase: Record<Kind, string> = {
  signals: "linked to a problem or candidate",
  problems: "linked to a signal or candidate",
  solutions: "with a linked problem",
};
const everyPhrase: Record<Kind, string> = {
  signals: "feeds a problem or candidate",
  problems: "has sources or candidates",
  solutions: "addresses a linked problem",
};

// Counts come from the loaded page only; say so whenever other pages may exist.
export function summary(
  kind: Kind,
  filter: Filter,
  unlinked: number,
  total: number,
  pageScoped: boolean,
) {
  if (total === 0) return null;
  if (filter === "unlinked")
    return `Showing the ${unlinked} ${unlinkedPhrase[kind]}.`;
  if (filter === "linked")
    return `Showing the ${total - unlinked} ${linkedPhrase[kind]}.`;
  const noun = kind === "solutions" ? "candidate" : kind.slice(0, -1);
  if (unlinked === 0)
    return `Every ${noun}${pageScoped ? " on this page" : " here"} ${
      everyPhrase[kind]
    }.`;
  return `${unlinked}${pageScoped ? " on this page" : ""} ${
    unlinkedPhrase[kind]
  }.`;
}
