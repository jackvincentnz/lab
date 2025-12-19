import { describe, expect, render, screen, test } from "../../test";
import { PlanPage } from "./PlanPage";

describe("PlanPage", async () => {
  test("renders table", async () => {
    render(<PlanPage />);

    expect(screen.getByText(/First Name/)).toBeInTheDocument();
  });
});
