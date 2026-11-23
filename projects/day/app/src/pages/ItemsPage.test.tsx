import {
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
} from "@lab/test-utils";
import type { Item } from "../api";
import { fakeDayApi, item, withDayApi } from "../test/fakes";
import { ItemsPage } from "./ItemsPage";

describe("ItemsPage", () => {
  it("shows a loader until the list arrives", () => {
    const api = fakeDayApi();
    api.listItems.mockReturnValue(new Promise<Item[]>(() => undefined));

    render(<ItemsPage />, { wrapper: withDayApi(api) });

    expect(screen.getByLabelText("Loading your list")).toBeInTheDocument();
  });

  it("shows the service's items", async () => {
    const api = fakeDayApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);

    render(<ItemsPage />, { wrapper: withDayApi(api) });

    expect(await screen.findAllByRole("listitem")).toHaveLength(2);
    expect(screen.getByText("Plan the week")).toBeInTheDocument();
    expect(screen.getByText("Book travel")).toBeInTheDocument();
  });

  it("says the list is empty when there are no items", async () => {
    render(<ItemsPage />, { wrapper: withDayApi(fakeDayApi([])) });

    expect(
      await screen.findByText(/Nothing on your list yet/),
    ).toBeInTheDocument();
  });

  it("loads the list again on retry after it failed to load", async () => {
    const user = userEvent.setup();
    const api = fakeDayApi();
    api.listItems
      .mockRejectedValueOnce(new Error("offline"))
      .mockResolvedValueOnce([item({ name: "Plan the week" })]);
    render(<ItemsPage />, { wrapper: withDayApi(api) });

    await user.click(await screen.findByRole("button", { name: "Retry" }));

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(
      screen.queryByText("Could not load your list"),
    ).not.toBeInTheDocument();
  });

  it("adds an item and shows the service's new list", async () => {
    const user = userEvent.setup();
    const added = item({ name: "Plan the week" });
    const api = fakeDayApi([]);
    api.addItem.mockResolvedValue(added);
    render(<ItemsPage />, { wrapper: withDayApi(api) });
    api.listItems.mockResolvedValue([added]);

    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(api.addItem).toHaveBeenCalledWith({ name: "Plan the week" });
    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "New item" })).toHaveValue("");
  });

  it("renames an item and shows the service's new list", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const renamed = { ...original, name: "Plan the month" };
    const api = fakeDayApi([original]);
    api.updateItem.mockResolvedValue(renamed);
    render(<ItemsPage />, { wrapper: withDayApi(api) });
    api.listItems.mockResolvedValue([renamed]);

    await user.click(
      await screen.findByRole("button", { name: "Rename Plan the week" }),
    );
    const name = screen.getByRole("textbox", { name: "Name" });
    await user.clear(name);
    await user.type(name, "Plan the month{Enter}");

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      name: "Plan the month",
    });
    expect(await screen.findByText("Plan the month")).toBeInTheDocument();
    expect(
      screen.queryByRole("textbox", { name: "Name" }),
    ).not.toBeInTheDocument();
  });

  it("changes an item's status and shows the service's new list", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const started = { ...original, status: "doing" as const };
    const api = fakeDayApi([original]);
    api.updateItem.mockResolvedValue(started);
    render(<ItemsPage />, { wrapper: withDayApi(api) });
    api.listItems.mockResolvedValue([started]);

    await user.selectOptions(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
      "Doing",
    );

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      status: "doing",
    });
    expect(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
    ).toHaveValue("doing");
  });

  it("sends one change at a time", async () => {
    const user = userEvent.setup();
    const api = fakeDayApi([]);
    api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withDayApi(api) });

    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}{Enter}",
    );

    expect(api.addItem).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
  });

  it("shows an error when a change fails", async () => {
    const user = userEvent.setup();
    const api = fakeDayApi([item({ name: "Plan the week" })]);
    api.updateItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withDayApi(api) });

    await user.selectOptions(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
      "Done",
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
  });

  it("clears a failed rename's error when the rename is cancelled", async () => {
    const user = userEvent.setup();
    const api = fakeDayApi([item({ name: "Plan the week" })]);
    api.updateItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withDayApi(api) });
    await user.click(
      await screen.findByRole("button", { name: "Rename Plan the week" }),
    );
    await user.type(
      screen.getByRole("textbox", { name: "Name" }),
      " soon{Enter}",
    );
    await screen.findByText("Could not save your change");

    await user.click(screen.getByRole("button", { name: "Cancel" }));

    expect(
      screen.queryByText("Could not save your change"),
    ).not.toBeInTheDocument();
  });

  it("keeps offering a retry when a change fails before the list loads", async () => {
    const user = userEvent.setup();
    const api = fakeDayApi();
    api.listItems.mockRejectedValue(new Error("offline"));
    api.addItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withDayApi(api) });
    await screen.findByRole("button", { name: "Retry" });

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Retry" })).toBeInTheDocument();
  });
});
