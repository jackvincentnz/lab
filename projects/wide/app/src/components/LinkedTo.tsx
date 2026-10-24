import { count } from "../lib/format";
import type { Entry, Kind } from "../lib/kinds";
import { icon } from "./icons";

export function LinkedTo({ kind, entry }: { kind: Kind; entry: Entry }) {
  const parts = [
    kind !== "problems" && entry.problems?.length
      ? count(entry.problems.length, "problem")
      : null,
    kind !== "solutions" && entry.solutions?.length
      ? count(entry.solutions.length, "candidate")
      : null,
    kind !== "signals" && entry.signals?.length
      ? count(entry.signals.length, "source")
      : null,
  ].filter(Boolean);
  if (parts.length === 0) {
    return (
      <span className="dim">
        {kind === "solutions" ? "No problem linked" : "Not yet refined"}
      </span>
    );
  }
  return (
    <span className="chip">
      {icon.arrow}
      {parts.join(" · ")}
    </span>
  );
}
