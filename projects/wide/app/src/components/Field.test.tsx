import { describe, expect, it, render, screen } from "@lab/test-utils";
import { Field, Fields } from "./Field";

describe("Field", () => {
  it("shows a recorded value", () => {
    render(<Field title="Impact" value="Weekly delays" hint="Say how often" />);

    expect(screen.getByRole("heading", { name: "Impact" })).toBeVisible();
    expect(screen.getByText("Weekly delays")).toBeVisible();
    expect(screen.queryByText("Not yet recorded")).not.toBeInTheDocument();
  });

  it.each([undefined, null, "  "])(
    "prompts with the guide when the value is %j",
    (value) => {
      render(<Field title="Impact" value={value} hint="Say how often" />);

      expect(screen.getByText("Not yet recorded")).toBeVisible();
      expect(
        screen.getByText("The guide suggests: Say how often"),
      ).toBeVisible();
    },
  );
});

describe("Fields", () => {
  it("shows each field of the spec from the record", () => {
    render(
      <Fields
        spec={[
          { key: "scope", title: "Scope", hint: "Say what is included" },
          { key: "effort", title: "Effort", hint: "Estimate a range" },
        ]}
        record={{ scope: "Landing page only", effort: null }}
      />,
    );

    expect(screen.getByText("Landing page only")).toBeVisible();
    expect(
      screen.getByText("The guide suggests: Estimate a range"),
    ).toBeVisible();
  });
});
