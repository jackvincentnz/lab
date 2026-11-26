import {
  afterEach,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  waitFor,
  within,
} from "@lab/test-utils";
import type { Item } from "../api";
import { fakeSlateApi, item, withSlateApi } from "../test/fakes";
import { layOutRowsVertically } from "../test/layout";
import { ItemsPage } from "./ItemsPage";
import { onlineManager } from "@tanstack/react-query";
import { TRPCError } from "@trpc/server";

function itemNames() {
  return screen
    .getAllByRole("listitem")
    .map((row) => within(row).getByRole("button", { name: /^Move / }))
    .map((handle) => handle.getAttribute("aria-label")!.slice("Move ".length));
}

async function moveWithKeyboard(
  user: ReturnType<typeof userEvent.setup>,
  name: string,
  direction: "[ArrowUp]" | "[ArrowDown]",
) {
  layOutRowsVertically();
  (await screen.findByRole("button", { name: `Move ${name}` })).focus();
  await user.keyboard(`[Space]${direction}[Space]`);
}

describe("ItemsPage", () => {
  afterEach(() => {
    onlineManager.setOnline(true);
  });

  it("shows a loader until the list arrives", () => {
    const api = fakeSlateApi();
    api.listItems.mockReturnValue(new Promise<Item[]>(() => undefined));

    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    expect(screen.getByLabelText("Loading your list")).toBeInTheDocument();
  });

  it("shows the service's items", async () => {
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);

    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    expect(await screen.findAllByRole("listitem")).toHaveLength(2);
    expect(screen.getByText("Plan the week")).toBeInTheDocument();
    expect(screen.getByText("Book travel")).toBeInTheDocument();
  });

  it("says the list is empty when there are no items", async () => {
    render(<ItemsPage />, { wrapper: withSlateApi(fakeSlateApi([])) });

    expect(
      await screen.findByText(/Nothing on your slate yet/),
    ).toBeInTheDocument();
  });

  it("loads the list again on retry after it failed to load", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi();
    api.listItems
      .mockRejectedValueOnce(new Error("offline"))
      .mockResolvedValueOnce([item({ name: "Plan the week" })]);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    await user.click(await screen.findByRole("button", { name: "Retry" }));

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(
      screen.queryByText("Could not load your list"),
    ).not.toBeInTheDocument();
  });

  it("adds an item and shows the service's new list", async () => {
    const user = userEvent.setup();
    const added = item({ name: "Plan the week" });
    const api = fakeSlateApi([]);
    api.addItem.mockResolvedValue(added);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
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
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue(renamed);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
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
      updatedAt: original.updatedAt,
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
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue(started);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([started]);

    await user.selectOptions(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
      "Doing",
    );

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      status: "doing",
      updatedAt: original.updatedAt,
    });
    expect(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
    ).toHaveValue("doing");
  });

  it("keeps the draft and shows the newer name when a rename is refused as changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const api = fakeSlateApi([original]);
    api.updateItem.mockRejectedValue(new TRPCError({ code: "CONFLICT" }));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([
      {
        ...original,
        name: "Plan the month",
        updatedAt: "2026-11-22T10:00:00.000Z",
      },
    ]);

    await user.click(
      await screen.findByRole("button", { name: "Rename Plan the week" }),
    );
    await user.type(
      screen.getByRole("textbox", { name: "Name" }),
      " soon{Enter}",
    );

    expect(
      await screen.findByText("Not saved: the item changed elsewhere"),
    ).toBeInTheDocument();
    expect(
      await screen.findByText(
        "Changed elsewhere. Current name: Plan the month",
      ),
    ).toBeInTheDocument();
  });

  it("shows the current status when a status change is refused as changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const api = fakeSlateApi([original]);
    api.updateItem.mockRejectedValue(new TRPCError({ code: "CONFLICT" }));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([
      {
        ...original,
        status: "done",
        updatedAt: "2026-11-22T10:00:00.000Z",
      },
    ]);

    await user.selectOptions(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
      "Doing",
    );

    expect(
      await screen.findByText("Not saved: the item changed elsewhere"),
    ).toBeInTheDocument();
    await waitFor(() =>
      expect(
        screen.getByRole("combobox", { name: "Status of Plan the week" }),
      ).toHaveValue("done"),
    );
    expect(
      screen.queryByText("Could not save your change"),
    ).not.toBeInTheDocument();
  });

  it("shows a moved item in its new place while the move saves", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
      item({ name: "Write the report" }),
    ]);
    api.moveItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    await moveWithKeyboard(user, "Write the report", "[ArrowUp]");

    expect(itemNames()).toEqual([
      "Plan the week",
      "Write the report",
      "Book travel",
    ]);
  });

  it("keeps focus on a moved item for the next keyboard move", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    api.moveItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    await moveWithKeyboard(user, "Plan the week", "[ArrowDown]");

    await waitFor(() =>
      expect(
        screen.getByRole("button", { name: "Move Plan the week" }),
      ).toHaveFocus(),
    );
  });

  it("moves an item and shows the service's new list", async () => {
    const user = userEvent.setup();
    const [first, second, third] = [
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
      item({ name: "Write the report" }),
    ];
    const api = fakeSlateApi([first, second, third]);
    api.moveItem.mockResolvedValue(first);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([third, second, first]);

    await moveWithKeyboard(user, "Plan the week", "[ArrowDown]");

    expect(api.moveItem).toHaveBeenCalledWith({
      id: first.id,
      neighbourId: second.id,
      position: "after",
    });
    await waitFor(() =>
      expect(itemNames()).toEqual([
        "Write the report",
        "Book travel",
        "Plan the week",
      ]),
    );
  });

  it("deletes an item, shows the service's new list and offers an undo", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const remaining = item({ name: "Book travel" });
    const api = fakeSlateApi([deleted, remaining]);
    api.deleteItem.mockResolvedValue(deleted);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([remaining]);

    await user.click(
      await screen.findByRole("button", { name: "Delete Plan the week" }),
    );

    expect(api.deleteItem).toHaveBeenCalledWith({ id: deleted.id });
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Deleted Plan the week",
    );
    await waitFor(() => expect(itemNames()).toEqual(["Book travel"]));
  });

  it("moves focus to the undo after a delete", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const api = fakeSlateApi([deleted]);
    api.deleteItem.mockResolvedValue(deleted);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([]);
    (
      await screen.findByRole("button", { name: "Delete Plan the week" })
    ).focus();

    await user.keyboard("{Enter}");

    expect(await screen.findByRole("button", { name: "Undo" })).toHaveFocus();
  });

  it("puts a deleted item back on undo", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const remaining = item({ name: "Book travel" });
    const api = fakeSlateApi([deleted, remaining]);
    api.deleteItem.mockResolvedValue(deleted);
    api.restoreItem.mockResolvedValue(deleted);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([remaining]);
    await user.click(
      await screen.findByRole("button", { name: "Delete Plan the week" }),
    );
    api.listItems.mockResolvedValue([deleted, remaining]);

    await user.click(await screen.findByRole("button", { name: "Undo" }));

    expect(api.restoreItem).toHaveBeenCalledWith({ id: deleted.id });
    await waitFor(() =>
      expect(itemNames()).toEqual(["Plan the week", "Book travel"]),
    );
    expect(
      screen.queryByRole("button", { name: "Undo" }),
    ).not.toBeInTheDocument();
  });

  it("shows an error and offers no undo when a delete fails", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.deleteItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    await user.click(
      await screen.findByRole("button", { name: "Delete Plan the week" }),
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Undo" }),
    ).not.toBeInTheDocument();
    expect(itemNames()).toEqual(["Plan the week"]);
  });

  it("shows an error and keeps the undo when an undo fails", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const api = fakeSlateApi([deleted]);
    api.deleteItem.mockResolvedValue(deleted);
    api.restoreItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([]);
    await user.click(
      await screen.findByRole("button", { name: "Delete Plan the week" }),
    );

    await user.click(await screen.findByRole("button", { name: "Undo" }));

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Undo" })).toBeEnabled();
  });

  it("stops offering the undo once another change starts", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const api = fakeSlateApi([deleted]);
    api.deleteItem.mockResolvedValue(deleted);
    api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([]);
    await user.click(
      await screen.findByRole("button", { name: "Delete Plan the week" }),
    );
    await screen.findByRole("button", { name: "Undo" });

    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Book travel{Enter}",
    );

    expect(
      screen.queryByRole("button", { name: "Undo" }),
    ).not.toBeInTheDocument();
  });

  it("stops offering the undo once dismissed", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const api = fakeSlateApi([deleted]);
    api.deleteItem.mockResolvedValue(deleted);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([]);
    await user.click(
      await screen.findByRole("button", { name: "Delete Plan the week" }),
    );

    await user.click(await screen.findByRole("button", { name: "Dismiss" }));

    expect(
      screen.queryByRole("button", { name: "Undo" }),
    ).not.toBeInTheDocument();
    expect(api.restoreItem).not.toHaveBeenCalled();
  });

  it("puts an item back and shows an error when a move fails", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    api.moveItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    await moveWithKeyboard(user, "Plan the week", "[ArrowDown]");

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });

  it("shows the service's list and an error when a change fails", async () => {
    const user = userEvent.setup();
    const removed = item({ name: "Plan the week" });
    const remaining = item({ name: "Book travel" });
    const api = fakeSlateApi([removed, remaining]);
    api.updateItem.mockRejectedValue(new Error("No such item"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([remaining]);

    await user.selectOptions(
      await screen.findByRole("combobox", { name: "Status of Plan the week" }),
      "Doing",
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(itemNames()).toEqual(["Book travel"]);
  });

  it("shows the service's order and an error when a move fails", async () => {
    const user = userEvent.setup();
    const [first, second, third] = [
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
      item({ name: "Write the report" }),
    ];
    const api = fakeSlateApi([first, second, third]);
    api.moveItem.mockRejectedValue(new Error("No such item"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([third, first]);

    await moveWithKeyboard(user, "Plan the week", "[ArrowDown]");

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(itemNames()).toEqual(["Write the report", "Plan the week"]);
  });

  it("keeps reaching the service while the browser is offline", async () => {
    const user = userEvent.setup();
    const added = item({ name: "Plan the week" });
    const api = fakeSlateApi([]);
    api.addItem.mockResolvedValue(added);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    const name = await screen.findByRole("textbox", { name: "New item" });
    onlineManager.setOnline(false);
    api.listItems.mockResolvedValue([added]);

    await user.type(name, "Plan the week{Enter}");

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
  });

  it("sends one change at a time", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([]);
    api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}{Enter}",
    );

    expect(api.addItem).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("button", { name: "Add" })).toBeDisabled();
  });

  it("takes no other change while a delete saves", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    api.deleteItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    await user.click(
      await screen.findByRole("button", { name: "Delete Plan the week" }),
    );

    expect(
      screen.getByRole("combobox", { name: "Status of Book travel" }),
    ).toBeDisabled();
  });

  it("shows an error and keeps what was typed when a change fails", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([]);
    api.addItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });

    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Plan the week{Enter}",
    );

    expect(
      await screen.findByText("Could not save your change"),
    ).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "New item" })).toHaveValue(
      "Plan the week",
    );
  });

  it("clears a failed rename's error when the rename is cancelled", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.updateItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
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
    const api = fakeSlateApi();
    api.listItems.mockRejectedValue(new Error("offline"));
    api.addItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
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
