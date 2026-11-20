import type { MockLink } from "@apollo/client/testing";
import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "@lab/test-utils";
import { ProblemDocument } from "../__generated__/graphql";
import {
  candidateFields,
  decisionFields,
  link,
  problemFields,
  questionFields,
  signalFields,
} from "../test/records";
import { ProblemPage } from "./ProblemPage";

const empty = { questions: [], decisions: [], signals: [], solutions: [] };

function problem(record: object | null) {
  return {
    request: { query: ProblemDocument, variables: { id: "problem-1" } },
    result: { data: { problem: record } },
  };
}

function show(mocks: MockLink.MockedResponse[]) {
  render(<ProblemPage />, {
    route: "/problems/problem-1",
    path: "/problems/:id",
    mockedProvider: { mocks },
  });
}

describe("ProblemPage", () => {
  it("shows loading until the problem arrives", async () => {
    show([problem({ ...problemFields(), ...empty })]);

    expect(screen.getByRole("status")).toHaveTextContent("Loading…");
    expect(
      await screen.findByRole("heading", { name: "Finding the demo guide" }),
    ).toBeVisible();
  });

  it("retries after a failure", async () => {
    show([
      {
        request: { query: ProblemDocument, variables: { id: "problem-1" } },
        error: new Error("Unavailable"),
      },
      problem({ ...problemFields(), ...empty }),
    ]);

    await userEvent.click(
      await screen.findByRole("button", { name: "Try again" }),
    );

    expect(
      await screen.findByRole("heading", { name: "Finding the demo guide" }),
    ).toBeVisible();
  });

  it("says when the link's id is malformed", async () => {
    show([
      {
        request: { query: ProblemDocument, variables: { id: "problem-1" } },
        result: {
          errors: [
            {
              message: "Malformed id: problem-1",
              extensions: { errorType: "BAD_REQUEST" },
            },
          ],
        },
      },
    ]);

    expect(await screen.findByText("Invalid problem link.")).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Try again" }),
    ).not.toBeInTheDocument();
  });

  it("says when the problem does not exist", async () => {
    show([problem(null)]);

    expect(await screen.findByText("Problem not found.")).toBeVisible();
  });

  it("shows the problem's fields, refinement and linked records", async () => {
    show([
      problem({
        ...problemFields({ desiredOutcome: null }),
        questions: [questionFields({ question: "Who is affected?" })],
        decisions: [decisionFields({ decision: "Keep the scope small" })],
        signals: [link({ signal: signalFields() }, "Observed directly")],
        solutions: [link({ solution: candidateFields() }, "Closer help")],
      }),
    ]);

    expect(
      await screen.findByRole("heading", { name: "Finding the demo guide" }),
    ).toBeVisible();
    expect(screen.getByText("1 source signal")).toBeVisible();
    expect(screen.getByText("1 candidate")).toBeVisible();
    expect(screen.getByText("Unknown frequency")).toBeVisible();
    expect(screen.getByText("Not yet recorded")).toBeVisible();
    expect(screen.getByText("Who is affected?")).toBeVisible();
    expect(screen.getByText("Keep the scope small")).toBeVisible();
    expect(
      screen.getByRole("link", { name: "Synthetic observation" }),
    ).toBeVisible();
    expect(
      screen.getByRole("link", { name: "Contextual guide" }),
    ).toBeVisible();
    expect(screen.getByText("Expected contribution")).toBeVisible();
  });

  it("says what is missing when nothing is linked", async () => {
    show([problem({ ...problemFields(), ...empty })]);

    expect(
      await screen.findByText("No source signals linked yet."),
    ).toBeVisible();
    expect(
      screen.getByText("No candidate solutions linked yet."),
    ).toBeVisible();
  });

  it("refreshes the record on request", async () => {
    show([
      problem({ ...problemFields(), ...empty }),
      problem({ ...problemFields({ title: "Renamed" }), ...empty }),
    ]);
    await screen.findByRole("heading", { name: "Finding the demo guide" });

    await userEvent.click(screen.getByRole("button", { name: "Refresh" }));

    expect(
      await screen.findByRole("heading", { name: "Renamed" }),
    ).toBeVisible();
  });
});
