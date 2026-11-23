import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  within,
} from "@lab/test-utils";
import { ItemsPage } from "./ItemsPage";
import type { Item } from "../api";
import { fakeDayApi, item, withDayApi } from "../test/fakes";

describe("ItemsPage", () => {
  it("shows a loader until the list arrives", () => {
    const api = fakeDayApi();
    api.listItems.mockReturnValue(new Promise<Item[]>(() => undefined));

    render(<ItemsPage />, { wrapper: withDayApi(api) });

    expect(screen.getByLabelText("Loading your list")).toBeInTheDocument();
  });

  it("shows items in the order the service returns them", async () => {
    const api = fakeDayApi([
      item({ name: "Plan the week" }),
      item({ name: "Write the report", status: "doing" }),
    ]);

    render(<ItemsPage />, { wrapper: withDayApi(api) });

    const rows = await screen.findAllByRole("listitem");
    expect(rows.map((row) => row.textContent)).toEqual([
      expect.stringContaining("Plan the week"),
      expect.stringContaining("Write the report"),
    ]);
    expect(screen.getByLabelText("Status of Write the report")).toHaveValue(
      "doing",
    );
  });

  it("says the list is empty when there are no items", async () => {
    const api = fakeDayApi([]);

    render(<ItemsPage />, { wrapper: withDayApi(api) });

    expect(
      await screen.findByText(/Nothing on your list yet/),
    ).toBeInTheDocument();
  });

  it("offers a retry when the list cannot load", async () => {
    const api = fakeDayApi();
    api.listItems
      .mockRejectedValueOnce(new Error("offline"))
      .mockResolvedValueOnce([item({ name: "Plan the week" })]);
    render(<ItemsPage />, { wrapper: withDayApi(api) });

    await userEvent.click(await screen.findByRole("button", { name: "Retry" }));

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(
      screen.queryByText("Could not load your list"),
    ).not.toBeInTheDocument();
  });

  it("adds an item by name and shows the service's new list", async () => {
    const added = item({ name: "Plan the week" });
    const api = fakeDayApi([]);
    api.addItem.mockResolvedValue(added);
    render(<ItemsPage />, { wrapper: withDayApi(api) });
    api.listItems.mockResolvedValue([added]);

    await userEvent.type(
      await screen.findByLabelText("New item"),
      "Plan the week{Enter}",
    );

    expect(api.addItem).toHaveBeenCalledWith({ name: "Plan the week" });
    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(screen.getByLabelText("New item")).toHaveValue("");
  });

  it("keeps the typed name when adding fails", async () => {
    const api = fakeDayApi([]);
    api.addItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withDayApi(api) });

    await userEvent.type(
      await screen.findByLabelText("New item"),
      "Plan the week{Enter}",
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(screen.getByLabelText("New item")).toHaveValue("Plan the week");
  });

  it("renames an item", async () => {
    const original = item({ name: "Plan the week" });
    const renamed = { ...original, name: "Plan the month" };
    const api = fakeDayApi([original]);
    api.updateItem.mockResolvedValue(renamed);
    render(<ItemsPage />, { wrapper: withDayApi(api) });
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

  it("changes an item's status", async () => {
    const original = item({ name: "Plan the week" });
    const started = { ...original, status: "doing" as const };
    const api = fakeDayApi([original]);
    api.updateItem.mockResolvedValue(started);
    render(<ItemsPage />, { wrapper: withDayApi(api) });
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

  it("sends one add while the first is still saving", async () => {
    const api = fakeDayApi([]);
    api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withDayApi(api) });

    await userEvent.type(
      await screen.findByLabelText("New item"),
      "Plan the week{Enter}{Enter}",
    );

    expect(api.addItem).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
  });

  it("keeps offering a retry when a save fails after the list failed to load", async () => {
    const api = fakeDayApi();
    api.listItems.mockRejectedValue(new Error("offline"));
    api.addItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withDayApi(api) });
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

  it("keeps the status when changing it fails", async () => {
    const api = fakeDayApi([item({ name: "Plan the week" })]);
    api.updateItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withDayApi(api) });

    await userEvent.selectOptions(
      await screen.findByLabelText("Status of Plan the week"),
      "Done",
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(screen.getByLabelText("Status of Plan the week")).toHaveValue(
      "todo",
    );
  });

  it("keeps the rename open when saving it fails", async () => {
    const api = fakeDayApi([item({ name: "Plan the week" })]);
    api.updateItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withDayApi(api) });

    await userEvent.click(
      await screen.findByRole("button", { name: "Rename Plan the week" }),
    );
    await userEvent.type(screen.getByLabelText("Name"), " soon{Enter}");

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(screen.getByLabelText("Name")).toHaveValue("Plan the week soon");
  });

  it("clears a failed rename's error when the rename is cancelled", async () => {
    const api = fakeDayApi([item({ name: "Plan the week" })]);
    api.updateItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withDayApi(api) });
    await userEvent.click(
      await screen.findByRole("button", { name: "Rename Plan the week" }),
    );
    await userEvent.type(screen.getByLabelText("Name"), " soon{Enter}");
    await screen.findByText("Could not save your change");

    await userEvent.click(screen.getByRole("button", { name: "Cancel" }));

    expect(
      screen.queryByText("Could not save your change"),
    ).not.toBeInTheDocument();
  });
});
