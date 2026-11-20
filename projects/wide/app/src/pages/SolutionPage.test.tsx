import type { MockLink } from "@apollo/client/testing";
import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "@lab/test-utils";
import { SolutionDocument } from "../__generated__/graphql";
import {
  candidateFields,
  link,
  problemFields,
  signalFields,
} from "../test/records";
import { SolutionPage } from "./SolutionPage";

const empty = { questions: [], decisions: [], signals: [], problems: [] };

function solution(record: object | null) {
  return {
    request: { query: SolutionDocument, variables: { id: "solution-1" } },
    result: { data: { solution: record } },
  };
}

function show(mocks: MockLink.MockedResponse[]) {
  render(<SolutionPage />, {
    route: "/solutions/solution-1",
    path: "/solutions/:id",
    mockedProvider: { mocks },
  });
}

describe("SolutionPage", () => {
  it("shows loading until the candidate arrives", async () => {
    show([solution({ ...candidateFields(), ...empty })]);

    expect(screen.getByRole("status")).toHaveTextContent("Loading…");
    expect(
      await screen.findByRole("heading", { name: "Contextual guide" }),
    ).toBeVisible();
  });

  it("retries after a failure", async () => {
    show([
      {
        request: { query: SolutionDocument, variables: { id: "solution-1" } },
        error: new Error("Unavailable"),
      },
      solution({ ...candidateFields(), ...empty }),
    ]);

    await userEvent.click(
      await screen.findByRole("button", { name: "Try again" }),
    );

    expect(
      await screen.findByRole("heading", { name: "Contextual guide" }),
    ).toBeVisible();
  });

  it("says when the link's id is malformed", async () => {
    show([
      {
        request: { query: SolutionDocument, variables: { id: "solution-1" } },
        result: {
          errors: [
            {
              message: "Malformed id: solution-1",
              extensions: { errorType: "BAD_REQUEST" },
            },
          ],
        },
      },
    ]);

    expect(await screen.findByText("Invalid candidate link.")).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Try again" }),
    ).not.toBeInTheDocument();
  });

  it("says when the candidate does not exist", async () => {
    show([solution(null)]);

    expect(await screen.findByText("Candidate not found.")).toBeVisible();
    expect(screen.getByRole("link", { name: "Candidates" })).toHaveAttribute(
      "href",
      "/solutions",
    );
  });

  it("shows the candidate's fields and linked records", async () => {
    show([
      solution({
        ...candidateFields({ effort: null }),
        ...empty,
        problems: [link({ problem: problemFields() }, "Removes searching")],
        signals: [link({ signal: signalFields() }, "Asked for directly")],
      }),
    ]);

    expect(
      await screen.findByRole("heading", { name: "Contextual guide" }),
    ).toBeVisible();
    expect(screen.getByText("1 problem")).toBeVisible();
    expect(screen.getByText("1 source signal")).toBeVisible();
    expect(screen.getByText("Only the landing page")).toBeVisible();
    expect(screen.getByText("Not yet recorded")).toBeVisible();
    expect(
      screen.getByRole("link", { name: "Finding the demo guide" }),
    ).toBeVisible();
    expect(screen.getByText("Expected contribution")).toBeVisible();
    expect(
      screen.getByRole("link", { name: "Synthetic observation" }),
    ).toBeVisible();
  });

  it("keeps an orphan candidate's missing problem visible", async () => {
    show([solution({ ...candidateFields(), ...empty })]);

    expect(await screen.findByText("Problem not yet linked")).toBeVisible();
    expect(screen.getByText("No source signals linked yet.")).toBeVisible();
  });

  it("refreshes the record on request", async () => {
    show([
      solution({ ...candidateFields(), ...empty }),
      solution({ ...candidateFields({ title: "Renamed" }), ...empty }),
    ]);
    await screen.findByRole("heading", { name: "Contextual guide" });

    await userEvent.click(screen.getByRole("button", { name: "Refresh" }));

    expect(
      await screen.findByRole("heading", { name: "Renamed" }),
    ).toBeVisible();
  });
});
