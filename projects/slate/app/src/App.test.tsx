import { expect, render, screen, test } from "@lab/test-utils";
import { App } from "./App";
import { fakeSlateApi, withSlateApi } from "./test/fakes";

test("shows the app name above the list", async () => {
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });

  expect(screen.getByRole("heading", { name: "Slate" })).toBeInTheDocument();
  expect(
    await screen.findByText(/Nothing on your list yet/),
  ).toBeInTheDocument();
});
