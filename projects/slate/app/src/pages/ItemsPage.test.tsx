import {
  act,
  afterEach,
  describe,
  expect,
  fireEvent,
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

function layOutWindow(width: number) {
  vi.stubGlobal("innerWidth", width);
  vi.spyOn(Element.prototype, "getBoundingClientRect").mockReturnValue(
    DOMRect.fromRect({ width: width - 200, height: 800 }),
  );
}

function panelWidth(panel: HTMLElement) {
  return panel.style.getPropertyValue("--item-panel-width");
}

describe("ItemsPage", () => {
  afterEach(() => {
    onlineManager.setOnline(true);
  });

  it("shows a loader until the list arrives", () => {
    const api = fakeSlateApi();
    api.listItems.mockReturnValue(new Promise<Item[]>(() => undefined));

    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });

    expect(screen.getByLabelText("Loading your list")).toBeInTheDocument();
  });

  it("shows the service's items", async () => {
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);

    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });

    expect(await screen.findAllByRole("listitem")).toHaveLength(2);
    expect(screen.getByText("Plan the week")).toBeInTheDocument();
    expect(screen.getByText("Book travel")).toBeInTheDocument();
  });

  it("says the list is empty when there are no items", async () => {
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([])),
    });

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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });

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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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

  it("adds a source and shows the service's", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const source = {
      kind: "link" as const,
      url: "https://example.com/session",
      label: "Planning session",
    };
    const sourced = {
      ...original,
      sources: [{ ...source, id: crypto.randomUUID() }],
    };
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue(sourced);
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([sourced]);
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Add a source" }),
    );
    await user.type(
      within(panel).getByRole("textbox", { name: "Link" }),
      source.url,
    );
    await user.type(
      within(panel).getByRole("textbox", { name: "Label" }),
      `${source.label}{Enter}`,
    );

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      addSources: [source],
      updatedAt: original.updatedAt,
    });
    expect(
      await within(panel).findByRole("link", { name: "Planning session" }),
    ).toHaveAttribute("href", source.url);
  });

  it("removes an item's source", async () => {
    const user = userEvent.setup();
    const sourceId = crypto.randomUUID();
    const original = item({
      name: "Plan the week",
      sources: [
        {
          id: sourceId,
          kind: "link",
          url: "https://example.com/session",
          label: "Planning session",
        },
      ],
    });
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue({ ...original, sources: [] });
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", {
        name: "Edit source Planning session",
      }),
    );
    await user.click(
      within(panel).getByRole("button", { name: "Remove source" }),
    );

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      removeSources: [sourceId],
      updatedAt: original.updatedAt,
    });
  });

  it("shows the newer description when a description edit is refused as changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({
      name: "Plan the week",
      description: "Before Friday",
    });
    const api = fakeSlateApi([original]);
    api.updateItem.mockRejectedValue(new TRPCError({ code: "CONFLICT" }));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    const started = { ...original, status: "in_progress" as const };
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue(started);
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([started]);
    const panel = await openItem(user, "Plan the week");

    await user.click(
      within(panel).getByRole("button", { name: "Status: To do" }),
    );
    await user.click(screen.getByRole("menuitem", { name: "In progress" }));

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      status: "in_progress",
      updatedAt: original.updatedAt,
    });
    expect(
      await within(panel).findByRole("button", { name: "Status: In progress" }),
    ).toBeInTheDocument();
  });

  it("shows the newer name when a rename is refused as changed elsewhere", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const api = fakeSlateApi([original]);
    api.updateItem.mockRejectedValue(new TRPCError({ code: "CONFLICT" }));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    await user.click(screen.getByRole("menuitem", { name: "In progress" }));

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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });

    await screen.findByRole("button", { name: "Open Plan the week" });

    expect(
      screen.queryByRole("complementary", { name: "Item details" }),
    ).not.toBeInTheDocument();
  });

  it("opens an item in the side panel", async () => {
    const user = userEvent.setup();
    render(<ItemsPage view="today" />, {
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
    render(<ItemsPage view="today" />, {
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
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    await openItem(user, "Plan the week");

    await user.keyboard("{Escape}");

    expect(
      screen.getByRole("button", { name: "Open Plan the week" }),
    ).toHaveFocus();
  });

  it("shows the side panel 440 pixels wide when no width is saved", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });

    const panel = await openItem(user, "Plan the week");

    expect(panelWidth(panel)).toBe("440px");
  });

  it("widens the side panel when its edge is dragged and saves the width", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    const panel = await openItem(user, "Plan the week");
    const edge = screen.getByRole("separator", { name: "Resize item details" });

    await user.pointer([
      { keys: "[MouseLeft>]", target: edge, coords: { clientX: 960 } },
      { coords: { clientX: 860 } },
      { keys: "[/MouseLeft]", coords: { clientX: 860 } },
    ]);

    expect(panelWidth(panel)).toBe("540px");
    expect(localStorage.getItem("slate.itemPanelWidth")).toBe("540");
  });

  it("widens the side panel from its edge with the keyboard", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    const panel = await openItem(user, "Plan the week");
    screen.getByRole("separator", { name: "Resize item details" }).focus();

    await user.keyboard("{ArrowLeft}");

    expect(panelWidth(panel)).toBe("450px");
  });

  it("narrows the side panel to no less than 360 pixels", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    const panel = await openItem(user, "Plan the week");
    screen.getByRole("separator", { name: "Resize item details" }).focus();

    await user.keyboard("{End}");

    expect(panelWidth(panel)).toBe("360px");
  });

  it("widens the side panel to no more than 60% of the window", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    const panel = await openItem(user, "Plan the week");
    screen.getByRole("separator", { name: "Resize item details" }).focus();

    await user.keyboard("{Home}");

    expect(panelWidth(panel)).toBe("960px");
  });

  it("shows the side panel at its saved width", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    localStorage.setItem("slate.itemPanelWidth", "520");
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });

    const panel = await openItem(user, "Plan the week");

    expect(panelWidth(panel)).toBe("520px");
  });

  it("narrows the side panel to fit a narrower window and keeps its saved width", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    localStorage.setItem("slate.itemPanelWidth", "900");
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    const panel = await openItem(user, "Plan the week");

    layOutWindow(1000);
    fireEvent(window, new Event("resize"));

    expect(panelWidth(panel)).toBe("600px");
    expect(localStorage.getItem("slate.itemPanelWidth")).toBe("900");
  });

  it("widens the side panel back to its saved width when the window widens", async () => {
    const user = userEvent.setup();
    layOutWindow(1000);
    localStorage.setItem("slate.itemPanelWidth", "900");
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    const panel = await openItem(user, "Plan the week");

    layOutWindow(1600);
    fireEvent(window, new Event("resize"));

    expect(panelWidth(panel)).toBe("900px");
  });

  it("tells assistive technology the side panel's width and limits", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });

    await openItem(user, "Plan the week");

    const edge = screen.getByRole("separator", { name: "Resize item details" });
    expect(edge).toHaveAttribute("aria-valuenow", "440");
    expect(edge).toHaveAttribute("aria-valuemin", "360");
    expect(edge).toHaveAttribute("aria-valuemax", "960");
  });

  it("keeps the saved width when the edge of a narrowed side panel is clicked", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    localStorage.setItem("slate.itemPanelWidth", "900");
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    await openItem(user, "Plan the week");
    layOutWindow(1000);
    fireEvent(window, new Event("resize"));

    await user.click(
      screen.getByRole("separator", { name: "Resize item details" }),
    );

    expect(localStorage.getItem("slate.itemPanelWidth")).toBe("900");
  });

  it("returns the side panel to 440 pixels when its edge is double-clicked", async () => {
    const user = userEvent.setup();
    layOutWindow(1600);
    localStorage.setItem("slate.itemPanelWidth", "700");
    render(<ItemsPage view="today" />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });
    const panel = await openItem(user, "Plan the week");

    await user.dblClick(
      screen.getByRole("separator", { name: "Resize item details" }),
    );

    expect(panelWidth(panel)).toBe("440px");
    expect(localStorage.getItem("slate.itemPanelWidth")).toBe("440");
  });

  it("closes the side panel when its item leaves the list", async () => {
    const user = userEvent.setup();
    const opened = item({ name: "Plan the week" });
    const other = item({ name: "Book travel" });
    const api = fakeSlateApi([opened, other]);
    api.updateItem.mockResolvedValue({ ...other, status: "done" });
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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

  it("keeps the side panel closed when an item that left the list comes back", async () => {
    const user = userEvent.setup();
    const opened = item({ name: "Plan the week" });
    const other = item({ name: "Book travel" });
    const api = fakeSlateApi([opened, other]);
    api.updateItem.mockResolvedValue({ ...opened, status: "delegated" });
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    const panel = await openItem(user, "Plan the week");
    api.listItems.mockResolvedValue([other]);
    await user.click(
      within(panel).getByRole("button", { name: "Status: To do" }),
    );
    await user.click(screen.getByRole("menuitem", { name: "Delegated" }));
    await waitFor(() =>
      expect(screen.queryByText("Plan the week")).not.toBeInTheDocument(),
    );
    api.listItems.mockResolvedValue([opened, { ...other, status: "done" }]);
    api.updateItem.mockResolvedValue({ ...other, status: "done" });

    await user.click(
      screen.getByRole("checkbox", { name: "Mark Book travel done" }),
    );

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(
      screen.queryByRole("complementary", { name: "Item details" }),
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });

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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });

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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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

  it("starts an item dragged into the empty In progress group and shows the service's new list", async () => {
    const user = userEvent.setup();
    const original = item({ name: "Plan the week" });
    const started = { ...original, status: "in_progress" as const };
    const api = fakeSlateApi([original]);
    api.updateItem.mockResolvedValue(started);
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([started]);

    await moveWithKeyboard(user, "Plan the week", "[ArrowUp]");

    expect(api.updateItem).toHaveBeenCalledWith({
      id: original.id,
      status: "in_progress",
      updatedAt: original.updatedAt,
    });
    expect(
      within(screen.getByRole("list", { name: "In progress" })).getByText(
        "Plan the week",
      ),
    ).toBeInTheDocument();
  });

  it("deletes an item, shows the service's new list and offers an undo", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const remaining = item({ name: "Book travel" });
    const api = fakeSlateApi([deleted, remaining]);
    api.deleteItem.mockResolvedValue(deleted);
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });

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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });

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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );

    await user.click(
      screen.getByRole("checkbox", { name: "Mark Book travel done" }),
    );

    expect(api.updateItem).not.toHaveBeenCalled();
  });

  it("takes no delete while a change saves", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    await openItem(user, "Plan the week");
    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Book travel{Enter}",
    );

    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );

    expect(api.deleteItem).not.toHaveBeenCalled();
  });

  it("takes no rename while a change saves", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Rename Plan the week" }),
    );
    await user.type(
      screen.getByRole("textbox", { name: "New item" }),
      "Book travel{Enter}",
    );

    await user.type(
      screen.getByRole("textbox", { name: "Name" }),
      " soon{Enter}",
    );

    expect(api.updateItem).not.toHaveBeenCalled();
  });

  it("puts a deleted item back only once when undo is clicked twice", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Plan the week" });
    const api = fakeSlateApi([deleted]);
    api.deleteItem.mockResolvedValue(deleted);
    api.restoreItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([]);
    await openItem(user, "Plan the week");
    await user.click(
      screen.getByRole("button", { name: "Delete Plan the week" }),
    );
    const undo = await screen.findByRole("button", { name: "Undo" });

    await user.dblClick(undo);

    expect(api.restoreItem).toHaveBeenCalledTimes(1);
  });

  it("keeps a saving move's order when another move is made", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
      item({ name: "Write the report" }),
    ]);
    api.moveItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    await moveWithKeyboard(user, "Write the report", "[ArrowUp]");

    await moveWithKeyboard(user, "Plan the week", "[ArrowDown]");

    expect(api.moveItem).toHaveBeenCalledTimes(1);
    expect(itemNames()).toEqual([
      "Plan the week",
      "Write the report",
      "Book travel",
    ]);
  });

  it("keeps the order when an item is moved while a change saves", async () => {
    const user = userEvent.setup();
    const api = fakeSlateApi([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    api.addItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    await user.type(
      await screen.findByRole("textbox", { name: "New item" }),
      "Write the report{Enter}",
    );

    await moveWithKeyboard(user, "Book travel", "[ArrowUp]");

    expect(api.moveItem).not.toHaveBeenCalled();
    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });

  it("keeps controls enabled while a change has been saving for less than 300 ms", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.updateItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    const checkbox = await screen.findByRole("checkbox", {
      name: "Mark Plan the week done",
    });
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    await user.click(checkbox);
    await act(() => vi.advanceTimersByTimeAsync(100));

    expect(checkbox).toBeEnabled();
  });

  it("shows controls as disabled once a change has been saving for 300 ms", async () => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);
    api.updateItem.mockReturnValue(new Promise<Item>(() => undefined));
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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

  it("enables controls again once a change that disabled them finishes saving", async () => {
    const original = item({ name: "Plan the week" });
    const done = { ...original, status: "done" as const };
    const api = fakeSlateApi([original]);
    let finishSave: (saved: Item) => void = () => undefined;
    api.updateItem.mockReturnValue(
      new Promise<Item>((resolve) => (finishSave = resolve)),
    );
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });

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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
    const panel = await openItem(user, "Plan the week");
    await user.click(
      within(panel).getByRole("button", { name: "Status: To do" }),
    );
    await user.click(screen.getByRole("menuitem", { name: "In progress" }));
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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
    render(<ItemsPage view="today" />, { wrapper: withSlateApi(api) });
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

describe("ItemsPage in another view", () => {
  it.each([
    ["today", {}],
    ["delegated", { statuses: ["delegated"] }],
    ["deferred", { statuses: ["deferred"] }],
    ["done", { statuses: ["done"], doneTodayOnly: false, orderBy: "doneAt" }],
    ["wontDo", { statuses: ["wont_do"] }],
    [
      "deleted",
      {
        deleted: true,
        statuses: [
          "todo",
          "in_progress",
          "done",
          "deferred",
          "wont_do",
          "delegated",
        ],
        doneTodayOnly: false,
        orderBy: "updatedAt",
      },
    ],
  ] as const)("lists the %s view's items", async (view, query) => {
    const api = fakeSlateApi([item({ name: "Plan the week" })]);

    render(<ItemsPage view={view} />, { wrapper: withSlateApi(api) });

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(api.listItems).toHaveBeenCalledWith(query);
  });

  it("offers neither adding nor moving outside Today", async () => {
    const api = fakeSlateApi([
      item({ name: "Plan the week", status: "deferred" }),
    ]);

    render(<ItemsPage view="deferred" />, { wrapper: withSlateApi(api) });

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(
      screen.queryByRole("textbox", { name: "New item" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Move Plan the week" }),
    ).not.toBeInTheDocument();
  });

  it("says when a view has no items", async () => {
    render(<ItemsPage view="delegated" />, {
      wrapper: withSlateApi(fakeSlateApi([])),
    });

    expect(await screen.findByText("Nothing delegated.")).toBeInTheDocument();
  });

  it.each([
    ["delegated", "Delegated"],
    ["deferred", "Deferred"],
    ["wontDo", "Won't do"],
    ["deleted", "Deleted"],
  ] as const)("names the %s view in a heading", async (view, name) => {
    render(<ItemsPage view={view} />, {
      wrapper: withSlateApi(fakeSlateApi([item({ name: "Plan the week" })])),
    });

    expect(await screen.findByText("Plan the week")).toBeInTheDocument();
    expect(screen.getByRole("heading", { level: 2, name })).toBeInTheDocument();
  });

  it("names a view in a heading when it has no items", async () => {
    render(<ItemsPage view="deferred" />, {
      wrapper: withSlateApi(fakeSlateApi([])),
    });

    expect(await screen.findByText("Nothing deferred.")).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { level: 2, name: "Deferred" }),
    ).toBeInTheDocument();
  });

  it.each([
    ["today", "Today", "todo"],
    ["done", "Done", "done"],
  ] as const)(
    "gives the %s view no heading of its own",
    async (view, name, status) => {
      const api = fakeSlateApi([
        item({
          name: "Plan the week",
          status,
          doneAt: status === "done" ? new Date().toISOString() : null,
        }),
      ]);

      render(<ItemsPage view={view} />, { wrapper: withSlateApi(api) });

      expect(await screen.findByText("Plan the week")).toBeInTheDocument();
      expect(screen.queryByRole("heading", { name })).not.toBeInTheDocument();
    },
  );

  it("groups done items by the day they were done", async () => {
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 10, 27, 15));
    const api = fakeSlateApi([
      item({
        name: "Ship the release",
        status: "done",
        doneAt: new Date(2026, 10, 27, 9).toISOString(),
      }),
      item({
        name: "Write the notes",
        status: "done",
        doneAt: new Date(2026, 10, 26, 17).toISOString(),
      }),
      item({
        name: "Book the room",
        status: "done",
        doneAt: new Date(2026, 10, 23, 11).toISOString(),
      }),
      item({
        name: "Order the cake",
        status: "done",
        doneAt: new Date(2026, 10, 23, 10).toISOString(),
      }),
    ]);

    render(<ItemsPage view="done" />, { wrapper: withSlateApi(api) });

    const days = await screen.findAllByRole("list");
    expect(
      days.map((day) => [
        day.getAttribute("aria-label"),
        within(day)
          .getAllByRole("listitem")
          .map((row) => row.textContent),
      ]),
    ).toEqual([
      ["Today", ["Ship the release"]],
      ["Yesterday", ["Write the notes"]],
      ["Monday, November 23", ["Book the room", "Order the cake"]],
    ]);
  });

  it("heads each day of done items with the day", async () => {
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 10, 27, 15));
    const api = fakeSlateApi([
      item({
        name: "Ship the release",
        status: "done",
        doneAt: new Date(2026, 10, 27, 9).toISOString(),
      }),
      item({
        name: "Write the notes",
        status: "done",
        doneAt: new Date(2026, 10, 26, 17).toISOString(),
      }),
    ]);

    render(<ItemsPage view="done" />, { wrapper: withSlateApi(api) });

    expect(await screen.findByText("Ship the release")).toBeInTheDocument();
    expect(
      screen
        .getAllByRole("heading", { level: 2 })
        .map((heading) => heading.textContent),
    ).toEqual(["Today", "Yesterday"]);
  });

  it("reopens a done item", async () => {
    const user = userEvent.setup();
    const done = item({
      name: "Ship the release",
      status: "done",
      doneAt: "2026-11-22T09:00:00.000Z",
    });
    const api = fakeSlateApi([done]);
    api.updateItem.mockResolvedValue({ ...done, status: "todo" });
    render(<ItemsPage view="done" />, { wrapper: withSlateApi(api) });

    await user.click(
      await screen.findByRole("checkbox", {
        name: "Mark Ship the release done",
      }),
    );

    expect(api.updateItem).toHaveBeenCalledWith({
      id: done.id,
      status: "todo",
      updatedAt: done.updatedAt,
    });
  });

  it("marks a delegated item done", async () => {
    const user = userEvent.setup();
    const delegated = item({ name: "Order monitors", status: "delegated" });
    const api = fakeSlateApi([delegated]);
    api.updateItem.mockResolvedValue({ ...delegated, status: "done" });
    render(<ItemsPage view="delegated" />, { wrapper: withSlateApi(api) });

    await user.click(
      await screen.findByRole("checkbox", { name: "Mark Order monitors done" }),
    );

    expect(api.updateItem).toHaveBeenCalledWith({
      id: delegated.id,
      status: "done",
      updatedAt: delegated.updatedAt,
    });
  });

  it("takes back a delegated item from its status menu", async () => {
    const user = userEvent.setup();
    const delegated = item({ name: "Order monitors", status: "delegated" });
    const api = fakeSlateApi([delegated]);
    api.updateItem.mockResolvedValue({ ...delegated, status: "todo" });
    render(<ItemsPage view="delegated" />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([]);
    const panel = await openItem(user, "Order monitors");

    await user.click(
      within(panel).getByRole("button", { name: "Status: Delegated" }),
    );
    await user.click(screen.getByRole("menuitem", { name: "To do" }));

    expect(api.updateItem).toHaveBeenCalledWith({
      id: delegated.id,
      status: "todo",
      updatedAt: delegated.updatedAt,
    });
    await waitFor(() =>
      expect(
        screen.queryByRole("complementary", { name: "Item details" }),
      ).not.toBeInTheDocument(),
    );
  });

  it("restores a deleted item", async () => {
    const user = userEvent.setup();
    const deleted = item({ name: "Try weekly digests" });
    const api = fakeSlateApi([deleted]);
    api.restoreItem.mockResolvedValue(deleted);
    render(<ItemsPage view="deleted" />, { wrapper: withSlateApi(api) });
    api.listItems.mockResolvedValue([]);

    await user.click(
      await screen.findByRole("button", { name: "Restore Try weekly digests" }),
    );

    expect(api.restoreItem).toHaveBeenCalledWith({ id: deleted.id });
    expect(await screen.findByText("Nothing deleted.")).toBeInTheDocument();
  });
});
