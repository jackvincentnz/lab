import { expect, render, screen, test } from "@lab/test-utils";
import { App } from "./App";

test("shows the app name", () => {
  render(<App />);

  expect(screen.getByRole("heading", { name: "Day" })).toBeInTheDocument();
});
