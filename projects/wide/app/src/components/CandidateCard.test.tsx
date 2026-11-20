import { describe, expect, it, render, screen } from "@lab/test-utils";
import { makeFragmentData } from "../__generated__";
import { CandidateFieldsFragmentDoc } from "../__generated__/graphql";
import { candidateFields, link } from "../test/records";
import { CandidateCard } from "./CandidateCard";

const solution = makeFragmentData(
  candidateFields({ id: "solution-7", title: "Contextual guide" }),
  CandidateFieldsFragmentDoc,
);

describe("CandidateCard", () => {
  it("links to the candidate and says why it is linked", () => {
    render(<CandidateCard link={link({ solution }, "Closer help")} />, {
      route: "/",
    });

    expect(
      screen.getByRole("link", { name: "Contextual guide" }),
    ).toHaveAttribute("href", "/solutions/solution-7");
    expect(screen.getByText("Proposed: offer help nearby")).toBeVisible();
    expect(screen.getByText("Why linked")).toBeVisible();
    expect(screen.getByText("Closer help")).toBeVisible();
  });

  it("labels the rationale as a contribution when asked", () => {
    render(
      <CandidateCard link={link({ solution }, "Closer help")} contribution />,
      { route: "/" },
    );

    expect(screen.getByText("Expected contribution")).toBeVisible();
  });
});
