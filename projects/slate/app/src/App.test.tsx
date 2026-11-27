import {
  expect,
  fireEvent,
  render,
  screen,
  test,
  userEvent,
  within,
} from "@lab/test-utils";
import { App } from "./App";
import { fakeSlateApi, item, withSlateApi } from "./test/fakes";

test("shows the app name above the list", async () => {
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });

  expect(screen.getByRole("heading", { name: "Slate" })).toBeInTheDocument();
  expect(
    await screen.findByText(/Nothing on your slate yet/),
  ).toBeInTheDocument();
});

test("offers the views in order, starting on Today", () => {
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });

  const views = within(screen.getByRole("navigation", { name: "Views" }));
  expect(views.getAllByRole("button").map((view) => view.textContent)).toEqual([
    "Today",
    "Delegated",
    "Deferred",
    "Done",
    "Won't do",
    "Deleted",
  ]);
  expect(views.getByRole("button", { name: "Today" })).toHaveAttribute(
    "aria-current",
    "page",
  );
});

test("shows the view chosen in the sidebar", async () => {
  const user = userEvent.setup();
  const api = fakeSlateApi();
  render(<App />, { wrapper: withSlateApi(api) });
  api.listItems.mockResolvedValue([
    item({ name: "Order monitors", status: "delegated" }),
  ]);

  await user.click(screen.getByRole("button", { name: "Delegated" }));

  expect(await screen.findByText("Order monitors")).toBeInTheDocument();
  expect(api.listItems).toHaveBeenLastCalledWith({ statuses: ["delegated"] });
  expect(screen.getByRole("button", { name: "Delegated" })).toHaveAttribute(
    "aria-current",
    "page",
  );
});

// The menu button shows only on a narrow window, and a hidden element has no accessible name.
function menuButton(label: string) {
  return screen.getByLabelText(label);
}

test("closes the views on a narrow window once a view is chosen", async () => {
  const user = userEvent.setup();
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });
  fireEvent.click(menuButton("Show views"));

  await user.click(screen.getByRole("button", { name: "Done" }));

  expect(menuButton("Show views")).toBeInTheDocument();
});

test("offers to hide the views on a narrow window while they show", () => {
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });

  fireEvent.click(menuButton("Show views"));

  expect(menuButton("Hide views")).toBeInTheDocument();
});
