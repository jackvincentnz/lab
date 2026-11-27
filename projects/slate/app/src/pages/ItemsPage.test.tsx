import {
  act,
  afterEach,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
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

async function openItem(
  user: ReturnType<typeof userEvent.setup>,
  name: string,
) {
  await user.click(await screen.findByRole("button", { name: `Open ${name}` }));
  return screen.getByRole("complementary", { name: "Item details" });
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
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Rename Plan the week" }),
    );
    const name = screen.getByRole("textbox", { name: "Name" });
    await user.clear(name);
    await user.type(name, "Plan the month{Enter}");

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      name: "Plan the month",
      updatedAt: original.updatedAt,
    });
    await waitFor(() => expect(itemNames()).toEqual(["Plan the month"]));
    expect(
      within(panel).getByRole("heading", { name: "Plan the month" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("textbox", { name: "Name" }),
    ).not.toBeInTheDocument();
  });

  it("saves an item's description and shows the service's", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const described = { ...original, description: "Before **Friday**" };
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue(described);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([described]);
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Add a description" }),
    );
    await user.type(
      within(panel).getByRole("textbox", { name: "Markdown" }),
      "Before **Friday**{Meta>}{Enter}{/Meta}",
    );

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      description: "Before **Friday**",
      updatedAt: original.updatedAt,
    });
    expect(await within(panel).findByText("Friday")).toBeInTheDocument();
    expect(
      within(panel).queryByRole("textbox", { name: "Markdown" }),
    ).not.toBeInTheDocument();
  });

  it("shows the newer description when a description edit is refused as changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({
      name: "Plan the week",
      description: "Before Friday",
    });
    const api = fakeSlateApi([original]);
    api.updateItem.mockRejectedValue(new TRPCError({ code: "CONFLICT" }));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([
      {
        ...original,
        description: "Before **Thursday**",
        updatedAt: "2026-11-22T10:00:00.000Z",
      },
    ]);
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Edit description" }),
    );
    const markdown = within(panel).getByRole("textbox", { name: "Markdown" });
    await user.type(markdown, " noon{Meta>}{Enter}{/Meta}");

    expect(
      await within(panel).findByText("Changed elsewhere. Current description:"),
    ).toBeInTheDocument();
    expect(within(panel).getByText("Thursday")).toBeInTheDocument();
    expect(markdown).toHaveValue("Before Friday noon");
  });

  it("changes an item's status and shows the service's new list", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const started = { ...original, status: "doing" as const };
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue(started);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([started]);
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Status: To do" }),
    );
    await user.click(screen.getByRole("menuitem", { name: "Doing" }));

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      status: "doing",
      updatedAt: original.updatedAt,
    });
    expect(
      await within(panel).findByRole("button", { name: "Status: Doing" }),
    ).toBeInTheDocument();
  });

  it("shows the newer name when a rename is refused as changed elsewhere", async () => {
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
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Rename Plan the week" }),
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
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Status: To do" }),
    );
    await user.click(screen.getByRole("menuitem", { name: "Doing" }));

    expect(
      await screen.findByText("Not saved: the item changed elsewhere"),
    ).toBeInTheDocument();
    expect(
      await within(panel).findByRole("button", { name: "Status: Done" }),
    ).toBeInTheDocument();
    expect(
      screen.queryByText("Could not save your change"),
    ).not.toBeInTheDocument();
  });

  it("marks an item done from its checkbox", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const done = { ...original, status: "done" as const };
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue(done);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([done]);

    await user.click(
      await screen.findByRole("checkbox", { name: "Mark Plan the week done" }),
    );

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      status: "done",
      updatedAt: original.updatedAt,
    });
    await waitFor(() =>
      expect(
        screen.getByRole("checkbox", { name: "Mark Plan the week done" }),
      ).toBeChecked(),
    );
  });

  it("shows no side panel until an item is opened", async () => {
    render(<ItemsPage />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });

    await screen.findByRole("button", { name: "Open Plan the week" });

    expect(
      screen.queryByRole("complementary", { name: "Item details" }),
    ).not.toBeInTheDocument();
  });

  it("opens an item in the side panel", async () => {
    const user = userEvent.setup();
    render(<ItemsPage />, {
      wrapper: withSlateApi(
        fakeSlateApi([
          item({ name: "Plan the week" }),
          item({ name: "Book travel" }),
        ]),
      ),
    });

    const panel = await openItem(user, "Book travel");

    expect(
      within(panel).getByRole("heading", { name: "Book travel" }),
    ).toBeInTheDocument();
  });

  it("closes the side panel", async () => {
    const user = userEvent.setup();
    render(<ItemsPage />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Close details" }),
    );

    expect(
      screen.queryByRole("complementary", { name: "Item details" }),
    ).not.toBeInTheDocument();
  });

  it("returns focus to the item's row when the side panel closes", async () => {
    const user = userEvent.setup();
    render(<ItemsPage />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    await openItem(user, "Plan the week");

    await user.keyboard("{Escape}");

    expect(
      screen.getByRole("button", { name: "Open Plan the week" }),
    ).toHaveFocus();
  });

  it("closes the side panel when its item leaves the list", async () => {
    const user = userEvent.setup();
    const opened = item({ name: "Plan the week" });
    const other = item({ name: "Book travel" });
    const api = fakeSlateApi([opened, other]);
    api.updateItem.mockResolvedValue({ ...other, status: "done" });
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    await openItem(user, "Plan the week");
    api.listItems.mockResolvedValue([{ ...other, status: "done" }]);

    await user.click(
      screen.getByRole("checkbox", { name: "Mark Book travel done" }),
    );

    await waitFor(() =>
      expect(
        screen.queryByRole("complementary", { name: "Item details" }),
      ).not.toBeInTheDocument(),
    );
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
    await openItem(user, "Plan the week");

    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );

    expect(api.deleteItem).toHaveBeenCalledWith({ id: deleted.id });
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Deleted Plan the week",
    );
    await waitFor(() => expect(itemNames()).toEqual(["Book travel"]));
    expect(
      screen.queryByRole("complementary", { name: "Item details" }),
    ).not.toBeInTheDocument();
  });

  it("moves focus to the undo after a delete", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const api = fakeSlateApi([deleted]);
    api.deleteItem.mockResolvedValue(deleted);
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([]);
    await openItem(user, "Plan the week");
    screen.getByRole("button", { name: "Delete Plan the week" }).focus();

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
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
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
    await openItem(user, "Plan the week");

    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
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
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
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
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
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
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
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

    await user.click(
      await screen.findByRole("checkbox", { name: "Mark Plan the week done" }),
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
  });

  it("takes no other change while a delete saves", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    api.deleteItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );

    await user.click(
      screen.getByRole("checkbox", { name: "Mark Book travel done" }),
    );

    expect(api.updateItem).not.toHaveBeenCalled();
  });

  it("keeps the order when an item is moved while a change saves", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Write the report{Enter}",
    );

    await moveWithKeyboard(user, "Book travel", "[ArrowUp]");

    expect(api.moveItem).not.toHaveBeenCalled();
    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });

  it("keeps controls enabled while a change has saved for less than 300 ms", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.updateItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    const checkbox = await screen.findByRole("checkbox", {
      name: "Mark Plan the week done",
    });
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    await user.click(checkbox);
    await act(() => vi.advanceTimersByTimeAsync(100));

    expect(checkbox).toBeEnabled();
  });

  it("shows controls as disabled once a change has saved for 300 ms", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.updateItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    const checkbox = await screen.findByRole("checkbox", {
      name: "Mark Plan the week done",
    });
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    await user.click(checkbox);
    await act(() => vi.advanceTimersByTimeAsync(300));

    expect(checkbox).toBeDisabled();
  });

  it("keeps a rename open when it is cancelled while it saves", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.updateItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
    );
    const name = screen.getByRole("textbox", { name: "Name" });
    await user.type(name, " soon{Enter}");

    await user.type(name, "{Escape}");

    expect(name).toBeInTheDocument();
    expect(name).toHaveValue("Plan the week soon");
  });

  it("enables controls again when a slow change finishes saving", async () => {
    const original = item({ name: "Plan the week" });
    const done = { ...original, status: "done" as const };
    const api = fakeSlateApi([original]);
    let finishSave: (saved: Item) => void = () => undefined;
    api.updateItem.mockReturnValue(
      new Promise<Item>((resolve) => (finishSave = resolve)),
    );
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    const checkbox = await screen.findByRole("checkbox", {
      name: "Mark Plan the week done",
    });
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });
    await user.click(checkbox);
    await act(() => vi.advanceTimersByTimeAsync(300));
    api.listItems.mockResolvedValue([done]);

    finishSave(done);
    await act(() => vi.advanceTimersByTimeAsync(0));

    expect(checkbox).toBeEnabled();
    expect(checkbox).toBeChecked();
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
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
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

  it("clears a failed change's error when another item is opened", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    api.updateItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    await user.click(
      await screen.findByRole("checkbox", { name: "Mark Plan the week done" }),
    );
    await screen.findByText("Could not save your change");

    await openItem(user, "Book travel");

    expect(
      screen.queryByText("Could not save your change"),
    ).not.toBeInTheDocument();
  });

  it("clears a failed change's error when the side panel closes", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.updateItem.mockRejectedValue(new Error("offline"));
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    const panel = await openItem(user, "Plan the week");
    await user.click(
      within(panel).getByRole("button", { name: "Status: To do" }),
    );
    await user.click(screen.getByRole("menuitem", { name: "Doing" }));
    await screen.findByText("Could not save your change");

    await user.click(
      within(panel).getByRole("button", { name: "Close details" }),
    );

    expect(
      screen.queryByText("Could not save your change"),
    ).not.toBeInTheDocument();
  });

  it("offers the undo for a delete that finishes after the side panel closed", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    let finishDelete!: (item: Item) => void;
    const api = fakeSlateApi([deleted]);
    api.deleteItem.mockReturnValue(
      new Promise<Item>((resolve) => {
        finishDelete = resolve;
      }),
    );
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );
    await user.click(screen.getByRole("button", { name: "Close details" }));

    act(() => finishDelete(deleted));

    expect(await screen.findByRole("button", { name: "Undo" })).toBeEnabled();
  });

  it("keeps another item open when a delete finishes after it was opened", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    let finishDelete!: (item: Item) => void;
    const api = fakeSlateApi([deleted, item({ name: "Book travel" })]);
    api.deleteItem.mockReturnValue(
      new Promise<Item>((resolve) => {
        finishDelete = resolve;
      }),
    );
    render(<ItemsPage />, { wrapper: withSlateApi(api) });
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );
    const panel = await openItem(user, "Book travel");

    act(() => finishDelete(deleted));

    expect(
      await within(panel).findByRole("heading", { name: "Book travel" }),
    ).toBeInTheDocument();
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
