import {
  expect,
  render,
  screen,
  test,
  userEvent,
  within,
} from "@lab/test-utils";
import { ItemList } from "./ItemList";
import type { Item } from "./api";
import { fakeDayApi, item, withDayApi } from "./test/fakes";

test("shows a loader until the list arrives", () => {
  const api = fakeDayApi();
  api.listItems.mockReturnValue(new Promise<Item[]>(() => undefined));

  render(<ItemList />, { wrapper: withDayApi(api) });

  expect(screen.getByLabelText("Loading your list")).toBeInTheDocument();
});

test("shows items in the order the service returns them", async () => {
  const api = fakeDayApi([
    item({ name: "Plan the week" }),
    item({ name: "Write the report", status: "doing" }),
  ]);

  render(<ItemList />, { wrapper: withDayApi(api) });

  const rows = await screen.findAllByRole("listitem");
  expect(rows.map((row) => row.textContent)).toEqual([
    expect.stringContaining("Plan the week"),
    expect.stringContaining("Write the report"),
  ]);
  expect(screen.getByLabelText("Status of Write the report")).toHaveValue(
    "doing",
  );
});

test("says the list is empty when there are no items", async () => {
  const api = fakeDayApi([]);

  render(<ItemList />, { wrapper: withDayApi(api) });

  expect(
    await screen.findByText(/Nothing on your list yet/),
  ).toBeInTheDocument();
});

test("offers a retry when the list cannot load", async () => {
  const api = fakeDayApi();
  api.listItems
    .mockRejectedValueOnce(new Error("offline"))
    .mockResolvedValueOnce([item({ name: "Plan the week" })]);
  render(<ItemList />, { wrapper: withDayApi(api) });

  await userEvent.click(await screen.findByRole("button", { name: "Retry" }));

  expect(await screen.findByText("Plan the week")).toBeInTheDocument();
  expect(
    screen.queryByText("Could not load your list"),
  ).not.toBeInTheDocument();
});

test("adds an item by name and shows the service's new list", async () => {
  const added = item({ name: "Plan the week" });
  const api = fakeDayApi([]);
  api.addItem.mockResolvedValue(added);
  render(<ItemList />, { wrapper: withDayApi(api) });
  api.listItems.mockResolvedValue([added]);

  await userEvent.type(
    await screen.findByLabelText("New item"),
    "Plan the week{Enter}",
  );

  expect(api.addItem).toHaveBeenCalledWith("Plan the week");
  expect(await screen.findByText("Plan the week")).toBeInTheDocument();
  expect(screen.getByLabelText("New item")).toHaveValue("");
});

test("keeps the typed name when adding fails", async () => {
  const api = fakeDayApi([]);
  api.addItem.mockRejectedValue(new Error("offline"));
  render(<ItemList />, { wrapper: withDayApi(api) });

  await userEvent.type(
    await screen.findByLabelText("New item"),
    "Plan the week{Enter}",
  );

  expect(
    await screen.findByText("Could not save your change"),
  ).toBeInTheDocument();
  expect(screen.getByLabelText("New item")).toHaveValue("Plan the week");
});

test("does not add a blank name", async () => {
  const api = fakeDayApi([]);
  render(<ItemList />, { wrapper: withDayApi(api) });

  await userEvent.type(await screen.findByLabelText("New item"), "   ");

  expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
});

test("renames an item", async () => {
  const original = item({ name: "Plan the week" });
  const renamed = { ...original, name: "Plan the month" };
  const api = fakeDayApi([original]);
  api.updateItem.mockResolvedValue(renamed);
  render(<ItemList />, { wrapper: withDayApi(api) });
  api.listItems.mockResolvedValue([renamed]);

  await userEvent.click(
    await screen.findByRole("button", { name: "Rename Plan the week" }),
  );
  const name = screen.getByLabelText("Name");
  await userEvent.clear(name);
  await userEvent.type(name, "Plan the month{Enter}");

  expect(api.updateItem).toHaveBeenCalledWith({
    id: original.id,
    name: "Plan the month",
  });
  expect(await screen.findByText("Plan the month")).toBeInTheDocument();
  expect(screen.queryByLabelText("Name")).not.toBeInTheDocument();
});

test("cancels a rename with Escape", async () => {
  const api = fakeDayApi([item({ name: "Plan the week" })]);
  render(<ItemList />, { wrapper: withDayApi(api) });

  await userEvent.click(
    await screen.findByRole("button", { name: "Rename Plan the week" }),
  );
  await userEvent.type(screen.getByLabelText("Name"), " later{Escape}");

  expect(api.updateItem).not.toHaveBeenCalled();
  expect(screen.getByText("Plan the week")).toBeInTheDocument();
});

test("changes an item's status", async () => {
  const original = item({ name: "Plan the week" });
  const started = { ...original, status: "doing" as const };
  const api = fakeDayApi([original]);
  api.updateItem.mockResolvedValue(started);
  render(<ItemList />, { wrapper: withDayApi(api) });
  api.listItems.mockResolvedValue([started]);

  await userEvent.selectOptions(
    await screen.findByLabelText("Status of Plan the week"),
    "Doing",
  );

  expect(api.updateItem).toHaveBeenCalledWith({
    id: original.id,
    status: "doing",
  });
  const row = screen.getByRole("listitem");
  expect(await within(row).findByDisplayValue("Doing")).toBeInTheDocument();
});

test("sends one add while the first is still saving", async () => {
  const api = fakeDayApi([]);
  api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
  render(<ItemList />, { wrapper: withDayApi(api) });

  await userEvent.type(
    await screen.findByLabelText("New item"),
    "Plan the week{Enter}{Enter}",
  );

  expect(api.addItem).toHaveBeenCalledTimes(1);
  expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
});

test("keeps offering a retry when a save fails after the list failed to load", async () => {
  const api = fakeDayApi();
  api.listItems.mockRejectedValue(new Error("offline"));
  api.addItem.mockRejectedValue(new Error("offline"));
  render(<ItemList />, { wrapper: withDayApi(api) });
  await screen.findByRole("button", { name: "Retry" });

  await userEvent.type(
    screen.getByLabelText("New item"),
    "Plan the week{Enter}",
  );

  expect(
    await screen.findByText("Could not save your change"),
  ).toBeInTheDocument();
  expect(screen.getByRole("button", { name: "Retry" })).toBeInTheDocument();
});

test("keeps the status when changing it fails", async () => {
  const api = fakeDayApi([item({ name: "Plan the week" })]);
  api.updateItem.mockRejectedValue(new Error("offline"));
  render(<ItemList />, { wrapper: withDayApi(api) });

  await userEvent.selectOptions(
    await screen.findByLabelText("Status of Plan the week"),
    "Done",
  );

  expect(
    await screen.findByText("Could not save your change"),
  ).toBeInTheDocument();
  expect(screen.getByLabelText("Status of Plan the week")).toHaveValue("todo");
});

test("strikes through done items", async () => {
  const api = fakeDayApi([
    item({ name: "Plan the week", status: "done" }),
    item({ name: "Book travel" }),
  ]);

  render(<ItemList />, { wrapper: withDayApi(api) });

  expect(await screen.findByText("Plan the week")).toHaveStyle({
    textDecoration: "line-through",
  });
  expect(screen.getByText("Book travel")).not.toHaveStyle({
    textDecoration: "line-through",
  });
});

test("cancels a rename with the Cancel button", async () => {
  const api = fakeDayApi([item({ name: "Plan the week" })]);
  render(<ItemList />, { wrapper: withDayApi(api) });

  await userEvent.click(
    await screen.findByRole("button", { name: "Rename Plan the week" }),
  );
  await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

  expect(screen.queryByLabelText("Name")).not.toBeInTheDocument();
  expect(screen.getByText("Plan the week")).toBeInTheDocument();
});

test("keeps the rename open when saving it fails", async () => {
  const api = fakeDayApi([item({ name: "Plan the week" })]);
  api.updateItem.mockRejectedValue(new Error("offline"));
  render(<ItemList />, { wrapper: withDayApi(api) });

  await userEvent.click(
    await screen.findByRole("button", { name: "Rename Plan the week" }),
  );
  await userEvent.type(screen.getByLabelText("Name"), " soon{Enter}");

  expect(
    await screen.findByText("Could not save your change"),
  ).toBeInTheDocument();
  expect(screen.getByLabelText("Name")).toHaveValue("Plan the week soon");
});
