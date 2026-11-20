import { describe, expect, it, render, screen } from "@lab/test-utils";
import { Rail } from "./Rail";

describe("Rail", () => {
  it("shows its items and note when it has any", () => {
    render(
      <Rail title="Sources" count={1} note="Read with care" empty="None yet">
        <p>Item</p>
      </Rail>,
    );

    expect(screen.getByText("Item")).toBeVisible();
    expect(screen.getByText("Read with care")).toBeVisible();
    expect(screen.queryByText("None yet")).not.toBeInTheDocument();
  });

  it("shows the empty message without a note when it has none", () => {
    render(
      <Rail title="Sources" count={0} note="Read with care" empty="None yet">
        <p>Item</p>
      </Rail>,
    );

    expect(screen.getByText("None yet")).toBeVisible();
    expect(screen.queryByText("Item")).not.toBeInTheDocument();
    expect(screen.queryByText("Read with care")).not.toBeInTheDocument();
  });
});
