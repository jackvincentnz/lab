import { describe, expect, it, render, screen } from "@lab/test-utils";
import { LinkedTo } from "./LinkedTo";

const links = (n: number) =>
  Array.from({ length: n }, (_, i) => ({ id: `link-${i}` }));

describe("LinkedTo", () => {
  it("counts a signal's problems and candidates", () => {
    render(
      <LinkedTo
        kind="signals"
        entry={{ id: "1", title: "", problems: links(1), solutions: links(2) }}
      />,
    );

    expect(screen.getByText("1 problem · 2 candidates")).toBeVisible();
  });

  it("counts a problem's sources and candidates", () => {
    render(
      <LinkedTo
        kind="problems"
        entry={{ id: "1", title: "", signals: links(2), solutions: links(1) }}
      />,
    );

    expect(screen.getByText("1 candidate · 2 sources")).toBeVisible();
  });

  it("counts a candidate's problems and sources", () => {
    render(
      <LinkedTo
        kind="solutions"
        entry={{ id: "1", title: "", problems: links(2), signals: links(1) }}
      />,
    );

    expect(screen.getByText("2 problems · 1 source")).toBeVisible();
  });

  it.each([
    ["signals", "Not yet refined"],
    ["problems", "Not yet refined"],
    ["solutions", "No problem linked"],
  ] as const)("says when a %s entry has no links", (kind, text) => {
    render(
      <LinkedTo
        kind={kind}
        entry={{ id: "1", title: "", signals: [], problems: [], solutions: [] }}
      />,
    );

    expect(screen.getByText(text)).toBeVisible();
  });
});
