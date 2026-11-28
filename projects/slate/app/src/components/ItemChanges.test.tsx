import { describe, expect, it, render, screen } from "@lab/test-utils";
import type { ItemChange } from "../api";
import { ItemChanges } from "./ItemChanges";

const now = new Date(2026, 10, 27, 15, 0);

function aChange(change: Partial<ItemChange>): ItemChange {
  return {
    at: new Date(2026, 10, 27, 10, 42).toISOString(),
    actor: "user",
    kind: "added",
    status: null,
    ...change,
  };
}

function time(hours: number, minutes: number) {
  return new Date(2026, 10, 27, hours, minutes).toLocaleTimeString(undefined, {
    hour: "2-digit",
    minute: "2-digit",
  });
}

describe("ItemChanges", () => {
  it("says who changed the item and when", () => {
    render(
      <ItemChanges
        now={now}
        changes={[
          aChange({ actor: "agent", kind: "rank" }),
          aChange({ actor: "user", kind: "status", status: "in_progress" }),
        ]}
      />,
    );

    expect(screen.getByText("History")).toBeInTheDocument();
    expect(screen.getByText(/An agent moved it/)).toHaveTextContent(
      `· ${time(10, 42)}`,
    );
    expect(screen.getByText(/You marked it in progress/)).toBeInTheDocument();
  });

  it.each([
    ["added", "added it"],
    ["name", "renamed it"],
    ["description", "changed the description"],
    ["sources", "changed the sources"],
    ["deleted", "deleted it"],
    ["restored", "restored it"],
  ] as const)("names a %s change", (kind, words) => {
    render(<ItemChanges now={now} changes={[aChange({ kind })]} />);

    expect(screen.getByText(new RegExp(`You ${words}`))).toBeInTheDocument();
  });

  it("names the day of a change before today", () => {
    render(
      <ItemChanges
        now={now}
        changes={[aChange({ at: new Date(2026, 10, 26, 9, 5).toISOString() })]}
      />,
    );

    expect(screen.getByText(/Yesterday/)).toBeInTheDocument();
  });

  it("shows nothing without changes", () => {
    render(<ItemChanges now={now} changes={[]} />);

    expect(screen.queryByText("History")).not.toBeInTheDocument();
  });
});
