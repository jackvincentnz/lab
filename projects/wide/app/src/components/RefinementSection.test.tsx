import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "@lab/test-utils";
import { makeFragmentData } from "../__generated__";
import {
  DecisionFieldsFragmentDoc,
  QuestionFieldsFragmentDoc,
} from "../__generated__/graphql";
import { decisionFields, questionFields } from "../test/records";
import { RefinementSection } from "./RefinementSection";

const question = (overrides: Parameters<typeof questionFields>[0]) =>
  makeFragmentData(questionFields(overrides), QuestionFieldsFragmentDoc);
const decision = (overrides: Parameters<typeof decisionFields>[0]) =>
  makeFragmentData(decisionFields(overrides), DecisionFieldsFragmentDoc);

describe("RefinementSection", () => {
  it("says when nothing has been recorded", () => {
    render(<RefinementSection record={{ questions: [], decisions: [] }} />);

    expect(screen.getByText("No decisions recorded yet.")).toBeVisible();
    expect(screen.getByText("No open questions recorded.")).toBeVisible();
    expect(screen.queryByText(/answered question/)).not.toBeInTheDocument();
  });

  it("shows a decision with its rationale, trigger and source", () => {
    render(
      <RefinementSection
        record={{
          questions: [],
          decisions: [
            decision({
              decision: "Focus on existing help",
              rationale: "Preserve the demo scope",
              revisitWhen: "The tour changes",
              source: "Demo owner",
            }),
          ],
        }}
      />,
    );

    expect(
      screen.getByRole("heading", { name: "Focus on existing help" }),
    ).toBeVisible();
    expect(screen.getByText("Preserve the demo scope")).toBeVisible();
    expect(screen.getByText("Reconsider when")).toBeVisible();
    expect(screen.getByText("The tour changes")).toBeVisible();
    expect(screen.getByText("Source: Demo owner")).toBeVisible();
  });

  it("leaves out a decision's unrecorded details", () => {
    render(
      <RefinementSection
        record={{
          questions: [],
          decisions: [decision({ decision: "Ship it" })],
        }}
      />,
    );

    expect(screen.getByRole("heading", { name: "Ship it" })).toBeVisible();
    expect(screen.queryByText("Reconsider when")).not.toBeInTheDocument();
    expect(screen.queryByText(/^Source:/)).not.toBeInTheDocument();
  });

  it("lists open questions and keeps a reopened question's earlier answer", () => {
    render(
      <RefinementSection
        record={{
          decisions: [],
          questions: [
            question({
              id: "q1",
              question: "How often does this happen?",
              context: "Frequency is unknown",
            }),
            question({
              id: "q2",
              question: "Which entrance matters?",
              answer: "Previously the landing page",
              source: "Synthetic review",
            }),
          ],
        }}
      />,
    );

    expect(screen.getByText("2 open questions")).toBeVisible();
    expect(screen.getByText("Frequency is unknown")).toBeVisible();
    expect(
      screen.getByText("Earlier answer · question reopened"),
    ).toBeVisible();
    expect(screen.getByText("Previously the landing page")).toBeVisible();
    expect(screen.getByText("Source: Synthetic review")).toBeVisible();
  });

  it("collapses answered questions until opened", async () => {
    render(
      <RefinementSection
        record={{
          decisions: [],
          questions: [
            question({
              question: "Who uses the demo?",
              status: "ANSWERED",
              answer: "New demo visitors",
            }),
          ],
        }}
      />,
    );

    expect(screen.getByText("No open questions recorded.")).toBeVisible();
    expect(screen.getByText("New demo visitors")).not.toBeVisible();

    await userEvent.click(screen.getByText("1 answered question"));

    expect(screen.getByText("Answer")).toBeVisible();
    expect(screen.getByText("New demo visitors")).toBeVisible();
  });
});
