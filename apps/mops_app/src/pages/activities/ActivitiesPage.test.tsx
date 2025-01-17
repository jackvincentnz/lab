import { describe, expect, render, screen, test } from "../../test";
import { ActivitiesPage } from "./ActivitiesPage";

describe("ActivitiesPage", async () => {
  test("renders", async () => {
    render(<ActivitiesPage />);

    expect(await screen.findByText(/Activities Page!/)).toBeInTheDocument();
  });
});
