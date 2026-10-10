import type { MockLink } from "@apollo/client/testing";
import {
  ProblemDocument,
  SignalDocument,
  SignalsDocument,
  SolutionDocument,
  StrategyDocument,
} from "./__generated__/graphql";
import { App } from "./App";
import { describe, expect, it, render, screen, userEvent } from "./test";

const time = "2026-01-01T00:00:00Z";
const signal = {
  __typename: "Signal",
  id: "signal-1",
  title: "Synthetic observation",
  content: "  Verbatim\nwith whitespace  ",
  source: "Synthetic fixture",
  capturedAt: time,
};
const candidate = {
  __typename: "Solution",
  id: "solution-1",
  title: "Contextual guide",
  approach: "Proposed: offer help nearby",
  scope: null,
  tradeoffs: "May complement navigation changes",
  effort: null,
  questions: [],
  decisions: [],
  updatedAt: time,
};
const problem = {
  __typename: "Problem",
  id: "problem-1",
  title: "Finding the demo guide",
  description: "Source statement: visitors have trouble finding help",
  impact: "Unknown frequency",
  desiredOutcome: "Proposed: make help easy to find",
  questions: [],
  decisions: [],
  updatedAt: time,
};
const sourceLink = {
  id: "link-1",
  rationale: "Observation supports this hypothesis",
  linkedAt: time,
  signal,
};
const solutionLink = {
  id: "link-2",
  rationale: "Hypothesis: closer help reduces searching",
  linkedAt: time,
  solution: candidate,
};

function show(route: string, mocks: MockLink.MockedResponse[]) {
  return render(<App />, { route, mockedProvider: { mocks } });
}

describe("WIDE explorer", () => {
  it("follows a source link from a problem and preserves original content", async () => {
    const user = userEvent.setup();
    show("/problems/problem-1", [
      {
        request: { query: ProblemDocument, variables: { id: "problem-1" } },
        result: {
          data: {
            problem: {
              ...problem,
              signals: [sourceLink],
              solutions: [solutionLink],
            },
          },
        },
      },
      {
        request: { query: SignalDocument, variables: { id: "signal-1" } },
        result: {
          data: { signal: { ...signal, problems: [], solutions: [] } },
        },
      },
    ]);
    expect(
      await screen.findByRole("heading", { name: problem.title }),
    ).toBeInTheDocument();
    expect(screen.getByText(solutionLink.rationale)).toBeInTheDocument();
    expect(screen.getByText("Expected contribution")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: candidate.title })).toHaveAttribute(
      "href",
      "/solutions/solution-1",
    );
    await user.click(screen.getByText("Read saved content"));
    expect(document.querySelector(".original")?.textContent).toBe(
      signal.content,
    );
    await user.click(screen.getByRole("link", { name: signal.title }));
    expect(
      await screen.findByRole("heading", { name: "Saved content" }),
    ).toBeInTheDocument();
    expect(document.querySelector(".original")?.textContent).toBe(
      signal.content,
    );
  });

  it("shows recorded decisions, open questions and retained answers without losing earlier answers", async () => {
    const user = userEvent.setup();
    show("/problems/problem-1", [
      {
        request: { query: ProblemDocument, variables: { id: "problem-1" } },
        result: {
          data: {
            problem: {
              ...problem,
              signals: [],
              solutions: [],
              questions: [
                {
                  __typename: "RefinementQuestion",
                  id: "q1",
                  question: "How often does this happen?",
                  status: "OPEN",
                  answer: null,
                  context: "Frequency is unknown",
                  source: null,
                  updatedAt: time,
                },
                {
                  __typename: "RefinementQuestion",
                  id: "q2",
                  question: "Who uses the demo?",
                  status: "ANSWERED",
                  answer: "New demo visitors",
                  context: null,
                  source: "User in synthetic refinement",
                  updatedAt: time,
                },
                {
                  __typename: "RefinementQuestion",
                  id: "q3",
                  question: "Which entrance matters?",
                  status: "OPEN",
                  answer: "Previously the landing page",
                  context: "New navigation changed this",
                  source: "Synthetic review",
                  updatedAt: time,
                },
              ],
              decisions: [
                {
                  __typename: "RefinementDecision",
                  id: "d1",
                  decision: "Focus on finding existing help",
                  rationale: "Preserve the demo scope",
                  source: "Demo owner",
                  revisitWhen: "The tour changes",
                  updatedAt: time,
                },
              ],
            },
          },
        },
      },
    ]);
    expect(
      await screen.findByText("Focus on finding existing help"),
    ).toBeVisible();
    expect(screen.getByText("Source: Demo owner")).toBeVisible();
    expect(screen.getByText("The tour changes")).toBeVisible();
    expect(screen.getByText("2 open questions")).toBeVisible();
    expect(
      screen.getByText("Earlier answer · question reopened"),
    ).toBeVisible();
    expect(screen.getByText("Previously the landing page")).toBeVisible();
    expect(screen.getByText("New demo visitors")).not.toBeVisible();
    await user.click(screen.getByText("1 answered question"));
    expect(screen.getByText("New demo visitors")).toBeVisible();
  });

  it("keeps orphan candidates and unknown effort visible", async () => {
    show("/solutions/solution-1", [
      {
        request: { query: SolutionDocument, variables: { id: "solution-1" } },
        result: {
          data: { solution: { ...candidate, signals: [], problems: [] } },
        },
      },
    ]);
    expect(
      await screen.findByText("Problem not yet linked"),
    ).toBeInTheDocument();
    expect(screen.getAllByText("Not yet recorded")).toHaveLength(2);
    expect(
      screen.getByText("No source signals linked yet."),
    ).toBeInTheDocument();
  });

  it("supports paging and search with an explicit empty result", async () => {
    const user = userEvent.setup();
    show("/signals", [
      {
        request: {
          query: SignalsDocument,
          variables: { page: 0, limit: 25, query: "" },
        },
        result: {
          data: {
            signals: Array.from({ length: 25 }, (_, i) => ({
              id: String(i),
              title: `Synthetic ${i}`,
              source: i % 2 ? "Synthetic fixture" : null,
              capturedAt: time,
              problems: i === 0 ? [{ id: "link-1" }] : [],
              solutions: [],
            })),
          },
        },
      },
      {
        request: {
          query: SignalsDocument,
          variables: { page: 1, limit: 25, query: "" },
        },
        result: { data: { signals: [] } },
      },
      {
        request: {
          query: SignalsDocument,
          variables: { page: 0, limit: 25, query: "absent" },
        },
        result: { data: { signals: [] } },
      },
    ]);
    await screen.findByText("Synthetic 24");
    expect(screen.getByText("1 problem")).toBeInTheDocument();
    expect(
      screen.getByText(
        "24 on this page not yet linked to a problem or candidate.",
      ),
    ).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Unlinked 24" }));
    expect(screen.queryByText("Synthetic 0")).not.toBeInTheDocument();
    expect(
      screen.getByText(
        "Showing the 24 not yet linked to a problem or candidate.",
      ),
    ).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Next" }));
    expect(
      await screen.findByText(
        "No more records on this page. Return to the previous page.",
      ),
    ).toBeInTheDocument();
    await user.type(
      screen.getByRole("textbox", { name: "Search signals" }),
      "absent{enter}",
    );
    expect(
      await screen.findByText("No signals match this search."),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Previous" }),
    ).not.toBeInTheDocument();
  });

  it("shows a retryable failure and an unset strategy", async () => {
    const user = userEvent.setup();
    show("/strategy", [
      { request: { query: StrategyDocument }, error: new Error("Unavailable") },
      {
        request: { query: StrategyDocument },
        result: {
          data: {
            strategy: { id: "strategy-1", content: null, updatedAt: null },
          },
        },
      },
    ]);
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Couldn’t load this view",
    );
    await user.click(screen.getByRole("button", { name: "Try again" }));
    expect(
      await screen.findByText(
        "No strategy has been set. Discuss and save it with your connected agent.",
      ),
    ).toBeInTheDocument();
  });

  it("uses the shared guides and does not load images embedded in stored text", async () => {
    const user = userEvent.setup();
    show("/strategy", [
      {
        request: { query: StrategyDocument },
        result: {
          data: {
            strategy: {
              id: "strategy-1",
              content: "![tracking](https://example.test/pixel.png)",
              updatedAt: time,
            },
          },
        },
      },
    ]);
    await screen.findByText("[Image: tracking]");
    expect(document.querySelector("img")).toBeNull();
    await user.click(screen.getByRole("button", { name: "Refinement guides" }));
    expect(
      await screen.findByRole("heading", { name: "Problem refinement guide" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("heading", {
        name: "Candidate solution refinement guide",
      }),
    ).toBeInTheDocument();
  });
});
