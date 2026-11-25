import { expect, render, screen, test } from "@lab/test-utils";
import { App } from "./App";
import { TestSlate, withSlate } from "./test/slate";

test("shows the app name above the list", async () => {
  render(<App />, { wrapper: withSlate(new TestSlate()) });

  expect(screen.getByRole("heading", { name: "Slate" })).toBeInTheDocument();
  expect(
    await screen.findByText(/Nothing on your slate yet/),
  ).toBeInTheDocument();
});
