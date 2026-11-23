import { expect, render, screen, test } from "@lab/test-utils";
import { App } from "./App";
import { fakeDayApi, withDayApi } from "./test/fakes";

test("shows the app name above the list", async () => {
  render(<App />, { wrapper: withDayApi(fakeDayApi()) });

  expect(screen.getByRole("heading", { name: "Day" })).toBeInTheDocument();
  expect(
    await screen.findByText(/Nothing on your list yet/),
  ).toBeInTheDocument();
});
