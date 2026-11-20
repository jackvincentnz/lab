import { describe, expect, it, render, screen } from "@lab/test-utils";
import { makeFragmentData } from "../__generated__";
import { ProblemFieldsFragmentDoc } from "../__generated__/graphql";
import { link, problemFields } from "../test/records";
import { ProblemCard } from "./ProblemCard";

const problem = makeFragmentData(
  problemFields({ id: "problem-7", title: "Finding help" }),
  ProblemFieldsFragmentDoc,
);

describe("ProblemCard", () => {
  it("links to the problem and says why it is linked", () => {
    render(<ProblemCard link={link({ problem }, "Shows the need")} />, {
      route: "/",
    });

    expect(screen.getByRole("link", { name: "Finding help" })).toHaveAttribute(
      "href",
      "/problems/problem-7",
    );
    expect(
      screen.getByText("Visitors have trouble finding help"),
    ).toBeVisible();
    expect(screen.getByText("Why linked")).toBeVisible();
    expect(screen.getByText("Shows the need")).toBeVisible();
  });

  it("labels the rationale as a contribution when asked", () => {
    render(
      <ProblemCard link={link({ problem }, "Shows the need")} contribution />,
      { route: "/" },
    );

    expect(screen.getByText("Expected contribution")).toBeVisible();
  });
});
