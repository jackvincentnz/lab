import {
  act,
  afterEach,
  expect,
  fireEvent,
  render,
  screen,
  test,
  userEvent,
  vi,
  waitFor,
  within,
} from "@lab/test-utils";
import { App } from "./App";
import { fakeSlateApi, item, withSlateApi } from "./test/fakes";

afterEach(() => {
  window.history.replaceState(null, "", "/");
});

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

async function openItemToDelete(user: ReturnType<typeof userEvent.setup>) {
  const deleted = item({ name: "Plan the week" });
  const api = fakeSlateApi([deleted]);
  api.deleteItem.mockResolvedValue(deleted);
  render(<App />, { wrapper: withSlateApi(api) });
  await user.click(
    await screen.findByRole("button", { name: "Open Plan the week" }),
  );
  api.listItems.mockResolvedValue([]);
  return screen.getByRole("button", { name: "Delete Plan the week" });
}

test("offers the undo for a deleted item over the page rather than in the list", async () => {
  const user = userEvent.setup();
  const deleteButton = await openItemToDelete(user);

  await user.click(deleteButton);

  const notice = await screen.findByRole("alert");
  expect(screen.getByRole("main")).not.toContainElement(notice);
});

test("stops offering the undo once another view is chosen", async () => {
  const user = userEvent.setup();
  await user.click(await openItemToDelete(user));
  await screen.findByRole("button", { name: "Undo" });

  await user.click(screen.getByRole("button", { name: "Deleted" }));

  await waitFor(() =>
    expect(
      screen.queryByRole("button", { name: "Undo" }),
    ).not.toBeInTheDocument(),
  );
});

test("keeps offering the undo while it has focus", async () => {
  vi.useFakeTimers({ shouldAdvanceTime: true });
  const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
  (await openItemToDelete(user)).focus();
  await user.keyboard("{Enter}");
  const undo = await screen.findByRole("button", { name: "Undo" });

  await act(() => vi.advanceTimersByTimeAsync(20_000));

  expect(undo).toHaveFocus();
});

test("searches every item from the header, in place of the view", async () => {
  const user = userEvent.setup();
  const api = fakeSlateApi();
  render(<App />, { wrapper: withSlateApi(api) });
  api.listItems.mockResolvedValue([
    item({ name: "Renew the domain", status: "done" }),
  ]);

  await user.type(
    screen.getByRole("searchbox", { name: "Search every item" }),
    " domain ",
  );

  expect(await screen.findByText("Renew the domain")).toBeInTheDocument();
  expect(api.listItems).toHaveBeenLastCalledWith(
    expect.objectContaining({ search: "domain" }),
  );
  expect(api.listItems).not.toHaveBeenCalledWith(
    expect.objectContaining({ search: "d" }),
  );
});

test("returns to the open view when the search is cleared with Escape", async () => {
  const user = userEvent.setup();
  const api = fakeSlateApi();
  render(<App />, { wrapper: withSlateApi(api) });
  await user.click(screen.getByRole("button", { name: "Deferred" }));
  const search = screen.getByRole("searchbox", { name: "Search every item" });
  await user.type(search, "domain");
  await screen.findByText("No items match “domain”.");

  await user.keyboard("{Escape}");

  expect(search).toHaveValue("");
  expect(await screen.findByText("Nothing deferred.")).toBeInTheDocument();
});

test("returns to the open view when the search is cleared with its button", async () => {
  const user = userEvent.setup();
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });
  await user.type(
    screen.getByRole("searchbox", { name: "Search every item" }),
    "domain",
  );
  await screen.findByText(/No items match/);

  await user.click(screen.getByRole("button", { name: "Clear search" }));

  expect(
    await screen.findByText(/Nothing on your slate yet/),
  ).toBeInTheDocument();
});

test("shows a view chosen in the sidebar during a search", async () => {
  const user = userEvent.setup();
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });
  const search = screen.getByRole("searchbox", { name: "Search every item" });
  await user.type(search, "domain");
  await screen.findByText(/No items match/);

  await user.click(screen.getByRole("button", { name: "Delegated" }));

  expect(search).toHaveValue("");
  expect(await screen.findByText("Nothing delegated.")).toBeInTheDocument();
});

test("focuses the search with Command K", async () => {
  const user = userEvent.setup();
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });

  await user.keyboard("{Meta>}k{/Meta}");

  expect(
    screen.getByRole("searchbox", { name: "Search every item" }),
  ).toHaveFocus();
});

test("shows the view, not the last results, while a new search waits", async () => {
  const user = userEvent.setup();
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });
  const search = screen.getByRole("searchbox", { name: "Search every item" });
  await user.type(search, "domain");
  await screen.findByText(/No items match/);

  await user.clear(search);
  await user.type(search, "x");

  expect(screen.queryByText(/No items match/)).not.toBeInTheDocument();
  expect(screen.getByText(/Nothing on your slate yet/)).toBeInTheDocument();
});

test("focuses the search with Command K from another text box", async () => {
  const user = userEvent.setup();
  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });
  await user.click(screen.getByRole("textbox", { name: "New item" }));

  await user.keyboard("{Meta>}k{/Meta}");

  expect(
    screen.getByRole("searchbox", { name: "Search every item" }),
  ).toHaveFocus();
});

function openLink(id: string) {
  window.history.replaceState(null, "", `/?item=${id}`);
}

test("opens a linked item in the view that lists it", async () => {
  const linked = item({ name: "Order monitors", status: "deferred" });
  const api = fakeSlateApi();
  api.listItems.mockImplementation((input) =>
    Promise.resolve(input?.deleted ? [] : [linked]),
  );
  openLink(linked.id);

  render(<App />, { wrapper: withSlateApi(api) });

  const panel = await screen.findByRole("complementary", {
    name: "Item details",
  });
  expect(
    within(panel).getByRole("heading", { name: "Order monitors" }),
  ).toBeInTheDocument();
  expect(screen.getByRole("button", { name: "Deferred" })).toHaveAttribute(
    "aria-current",
    "page",
  );
});

test("opens a deleted linked item in the Deleted view", async () => {
  const linked = item({ name: "Order monitors" });
  const api = fakeSlateApi();
  api.listItems.mockImplementation((input) =>
    Promise.resolve(input?.deleted ? [linked] : []),
  );
  openLink(linked.id);

  render(<App />, { wrapper: withSlateApi(api) });

  expect(
    await screen.findByRole("complementary", { name: "Item details" }),
  ).toBeInTheDocument();
  expect(screen.getByRole("button", { name: "Deleted" })).toHaveAttribute(
    "aria-current",
    "page",
  );
});

test("starts on Today when a linked item is not found", async () => {
  openLink("missing");

  render(<App />, { wrapper: withSlateApi(fakeSlateApi()) });

  expect(
    await screen.findByText(/Nothing on your slate yet/),
  ).toBeInTheDocument();
  expect(screen.getByRole("button", { name: "Today" })).toHaveAttribute(
    "aria-current",
    "page",
  );
  expect(
    screen.queryByRole("complementary", { name: "Item details" }),
  ).not.toBeInTheDocument();
});
