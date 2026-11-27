import {
  act,
  describe,
  expect,
  it,
  render,
  screen,
  userEvent,
  vi,
  within,
} from "@lab/test-utils";
import type { Item } from "../api";
import { item } from "../test/fakes";
import { layOutRowsVertically } from "../test/layout";
import { ItemList } from "./ItemList";

type ItemListProps = Parameters<typeof ItemList>[0];

function renderList(items: Item[]) {
  const onMove = vi.fn<ItemListProps["onMove"]>().mockReturnValue(true);
  const onStatusChange = vi
    .fn<ItemListProps["onStatusChange"]>()
    .mockReturnValue(true);
  render(
    <ItemList
      items={items}
      disabled={false}
      openId={undefined}
      onMove={onMove}
      onOpen={vi.fn()}
      onStatusChange={onStatusChange}
    />,
  );
  return { onMove, onStatusChange };
}

function rowsIn(group: string) {
  return within(screen.getByRole("list", { name: group }))
    .getAllByRole("listitem")
    .map((row) => row.textContent);
}

function daysAgo(days: number) {
  return new Date(Date.now() - days * 24 * 60 * 60 * 1000).toISOString();
}

function itemNames() {
  return screen
    .getAllByRole("button", { name: /^Move / })
    .map((handle) => handle.getAttribute("aria-label")!.slice("Move ".length));
}

function ranks() {
  return screen.getAllByText(/^#\d+$/).map((rank) => rank.textContent);
}

async function startDrag(name: string) {
  const user = userEvent.setup();
  layOutRowsVertically();
  screen.getByRole("button", { name: `Move ${name}` }).focus();
  await user.keyboard("[Space]");
  return user;
}

async function dragWithKeyboard(name: string, keys: string) {
  const user = await startDrag(name);
  await user.keyboard(`${keys}[Space]`);
}

describe("ItemList", () => {
  it("pins items in progress above the rest, each ranked in the one order", () => {
    renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel", status: "in_progress" }),
      item({ name: "Write the report" }),
      item({ name: "Call the bank", status: "in_progress" }),
    ]);

    expect(rowsIn("In progress")).toEqual([
      expect.stringMatching(/^#2Book travel/),
      expect.stringMatching(/^#4Call the bank/),
    ]);
    expect(rowsIn("Next")).toEqual([
      expect.stringMatching(/^#1Plan the week/),
      expect.stringMatching(/^#3Write the report/),
    ]);
  });

  it("warns when more than 3 items are in progress", () => {
    renderList(
      Array.from({ length: 4 }, () => item({ status: "in_progress" as const })),
    );

    expect(screen.getByText("Over your limit of 3")).toBeInTheDocument();
  });

  it("does not warn when 3 items are in progress", () => {
    renderList(
      Array.from({ length: 3 }, () => item({ status: "in_progress" as const })),
    );

    expect(screen.queryByText("Over your limit of 3")).not.toBeInTheDocument();
  });

  it("says when an item in progress has not changed for 5 days", () => {
    renderList([
      item({
        name: "Plan the week",
        status: "in_progress",
        updatedAt: daysAgo(6),
      }),
      item({
        name: "Book travel",
        status: "in_progress",
        updatedAt: daysAgo(4),
      }),
      item({ name: "Write the report", updatedAt: daysAgo(9) }),
    ]);

    expect(rowsIn("In progress")).toEqual([
      expect.stringContaining("Plan the weekNo change for 6 days"),
      expect.not.stringContaining("No change"),
    ]);
    expect(rowsIn("Next")).toEqual([expect.not.stringContaining("No change")]);
  });

  it("starts an item dragged into the In progress group, keeping its rank", async () => {
    const bookTravel = item({ name: "Book travel" });
    const { onMove, onStatusChange } = renderList([
      item({ name: "Plan the week", status: "in_progress" }),
      item({ name: "Write the report" }),
      bookTravel,
    ]);

    await dragWithKeyboard("Book travel", "[ArrowUp][ArrowUp]");

    expect(onMove).not.toHaveBeenCalled();
    expect(onStatusChange).toHaveBeenCalledWith(
      bookTravel,
      "in_progress",
      expect.any(Function),
    );
    expect(rowsIn("In progress")).toEqual([
      expect.stringMatching(/^#1Plan the week/),
      expect.stringMatching(/^#3Book travel/),
    ]);
    expect(rowsIn("Next")).toEqual([
      expect.stringMatching(/^#2Write the report/),
    ]);
  });

  it("starts a done item dragged into the In progress group", async () => {
    const bookTravel = item({ name: "Book travel", status: "done" });
    const { onStatusChange } = renderList([
      item({ name: "Plan the week", status: "in_progress" }),
      bookTravel,
    ]);

    await dragWithKeyboard("Book travel", "[ArrowUp]");

    expect(onStatusChange).toHaveBeenCalledWith(
      bookTravel,
      "in_progress",
      expect.any(Function),
    );
  });

  it("stops an item dragged out of the In progress group, keeping its rank", async () => {
    const planTheWeek = item({ name: "Plan the week", status: "in_progress" });
    const { onMove, onStatusChange } = renderList([
      planTheWeek,
      item({ name: "Book travel", status: "in_progress" }),
      item({ name: "Write the report" }),
    ]);

    await dragWithKeyboard("Plan the week", "[ArrowDown][ArrowDown]");

    expect(onMove).not.toHaveBeenCalled();
    expect(onStatusChange).toHaveBeenCalledWith(
      planTheWeek,
      "todo",
      expect.any(Function),
    );
    expect(rowsIn("In progress")).toEqual([
      expect.stringMatching(/^#2Book travel/),
    ]);
    expect(rowsIn("Next")).toEqual([
      expect.stringMatching(/^#1Plan the week/),
      expect.stringMatching(/^#3Write the report/),
    ]);
  });

  it("starts an item dragged into the empty In progress group", async () => {
    const bookTravel = item({ name: "Book travel" });
    const { onStatusChange } = renderList([
      item({ name: "Plan the week" }),
      bookTravel,
    ]);

    await dragWithKeyboard("Book travel", "[ArrowUp][ArrowUp]");

    expect(onStatusChange).toHaveBeenCalledWith(
      bookTravel,
      "in_progress",
      expect.any(Function),
    );
    expect(rowsIn("In progress")).toEqual([
      expect.stringMatching(/^#2Book travel/),
    ]);
  });

  it("stops an item dragged into the empty Next group", async () => {
    const planTheWeek = item({ name: "Plan the week", status: "in_progress" });
    const { onStatusChange } = renderList([planTheWeek]);

    await dragWithKeyboard("Plan the week", "[ArrowDown]");

    expect(onStatusChange).toHaveBeenCalledWith(
      planTheWeek,
      "todo",
      expect.any(Function),
    );
    expect(rowsIn("Next")).toEqual([expect.stringMatching(/^#1Plan the week/)]);
  });

  it("shows an item dragged into the In progress group at its rank there before it is dropped", async () => {
    renderList([
      item({ name: "Book travel" }),
      item({ name: "Plan the week", status: "in_progress" }),
    ]);
    const user = await startDrag("Book travel");

    await user.keyboard("[ArrowUp]");

    expect(rowsIn("In progress")).toEqual([
      expect.stringMatching(/^#1Book travel/),
      expect.stringMatching(/^#2Plan the week/),
    ]);
  });

  it("shows an item dragged out of the In progress group at its rank in Next before it is dropped", async () => {
    renderList([
      item({ name: "Write the report" }),
      item({ name: "Plan the week", status: "in_progress" }),
      item({ name: "Book travel" }),
    ]);
    const user = await startDrag("Plan the week");

    await user.keyboard("[ArrowDown]");

    expect(rowsIn("Next")).toEqual([
      expect.stringMatching(/^#1Write the report/),
      expect.stringMatching(/^#2Plan the week/),
      expect.stringMatching(/^#3Book travel/),
    ]);
  });

  it("leaves a done item done when it is dragged into In progress and back", async () => {
    const { onMove, onStatusChange } = renderList([
      item({ name: "Plan the week", status: "in_progress" }),
      item({ name: "Book travel", status: "done" }),
    ]);

    await dragWithKeyboard("Book travel", "[ArrowUp][ArrowDown][ArrowDown]");

    expect(onMove).not.toHaveBeenCalled();
    expect(onStatusChange).not.toHaveBeenCalled();
  });

  it("shows the items in the order given", () => {
    render(
      <ItemList
        items={[item({ name: "Plan the week" }), item({ name: "Book travel" })]}
        disabled={false}
        openId={undefined}
        onMove={vi.fn()}
        onOpen={vi.fn()}
        onStatusChange={vi.fn()}
      />,
    );

    expect(rowsIn("Next")).toEqual([
      expect.stringContaining("Plan the week"),
      expect.stringContaining("Book travel"),
    ]);
  });

  it("ranks the items from 1 in the order shown, done items included", () => {
    renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel", status: "done" }),
      item({ name: "Write the report" }),
    ]);

    expect(ranks()).toEqual(["#1", "#2", "#3"]);
    expect(itemNames()).toEqual([
      "Plan the week",
      "Book travel",
      "Write the report",
    ]);
  });

  it("ranks a dropped item by its new place until the move settles", async () => {
    renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);

    await dragWithKeyboard("Plan the week", "[ArrowDown]");

    expect(ranks()).toEqual(["#1", "#2"]);
    expect(itemNames()).toEqual(["Book travel", "Plan the week"]);
  });

  it("reports which item a change is for", async () => {
    const user = userEvent.setup();
    const bookTravel = item({ name: "Book travel" });
    const onStatusChange = vi.fn<ItemListProps["onStatusChange"]>();
    const onOpen = vi.fn<ItemListProps["onOpen"]>();
    render(
      <ItemList
        items={[item({ name: "Plan the week" }), bookTravel]}
        disabled={false}
        openId={undefined}
        onMove={vi.fn()}
        onOpen={onOpen}
        onStatusChange={onStatusChange}
      />,
    );

    await user.click(
      screen.getByRole("checkbox", { name: "Mark Book travel done" }),
    );
    await user.click(screen.getByRole("button", { name: "Open Book travel" }));

    expect(onStatusChange).toHaveBeenCalledWith(bookTravel, "done");
    expect(onOpen).toHaveBeenCalledWith(bookTravel);
  });

  it("shows which item is open", () => {
    const bookTravel = item({ name: "Book travel" });
    render(
      <ItemList
        items={[item({ name: "Plan the week" }), bookTravel]}
        disabled={false}
        openId={bookTravel.id}
        onMove={vi.fn()}
        onOpen={vi.fn()}
        onStatusChange={vi.fn()}
      />,
    );

    expect(
      screen.getByRole("button", { name: "Open Book travel" }),
    ).toHaveAttribute("aria-expanded", "true");
    expect(
      screen.getByRole("button", { name: "Open Plan the week" }),
    ).toHaveAttribute("aria-expanded", "false");
  });

  it("reports an item moved down as after the item it lands on", async () => {
    const planTheWeek = item({ name: "Plan the week" });
    const bookTravel = item({ name: "Book travel" });
    const { onMove } = renderList([planTheWeek, bookTravel, item()]);

    await dragWithKeyboard("Plan the week", "[ArrowDown]");

    expect(onMove).toHaveBeenCalledWith(
      {
        id: planTheWeek.id,
        neighbourId: bookTravel.id,
        position: "after",
      },
      expect.any(Function),
    );
  });

  it("reports an item moved up as before the item it lands on", async () => {
    const planTheWeek = item({ name: "Plan the week" });
    const bookTravel = item({ name: "Book travel" });
    const { onMove } = renderList([planTheWeek, bookTravel]);

    await dragWithKeyboard("Book travel", "[ArrowUp]");

    expect(onMove).toHaveBeenCalledWith(
      {
        id: bookTravel.id,
        neighbourId: planTheWeek.id,
        position: "before",
      },
      expect.any(Function),
    );
  });

  it("reports nothing when an item is dropped where it started", async () => {
    const { onMove } = renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);

    await dragWithKeyboard("Plan the week", "");

    expect(onMove).not.toHaveBeenCalled();
  });

  it("shows a dropped item in its new place until the move settles", async () => {
    renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);

    await dragWithKeyboard("Plan the week", "[ArrowDown]");

    expect(itemNames()).toEqual(["Book travel", "Plan the week"]);
  });

  it("ignores a drop of an item that left the list during the drag", async () => {
    const planTheWeek = item({ name: "Plan the week" });
    const bookTravel = item({ name: "Book travel" });
    const onMove = vi.fn<ItemListProps["onMove"]>();
    const list = (items: Item[]) => (
      <ItemList
        items={items}
        disabled={false}
        openId={undefined}
        onMove={onMove}
        onOpen={vi.fn()}
        onStatusChange={vi.fn()}
      />
    );
    const { rerender } = render(list([planTheWeek, bookTravel]));
    const user = await startDrag("Plan the week");
    await user.keyboard("[ArrowDown]");

    rerender(list([bookTravel]));
    await user.keyboard("[Space]");

    expect(onMove).not.toHaveBeenCalled();
  });

  it("puts an item back in its group when its drag is cancelled", async () => {
    renderList([
      item({ name: "Plan the week", status: "in_progress" }),
      item({ name: "Book travel" }),
    ]);
    const user = await startDrag("Book travel");
    await user.keyboard("[ArrowUp]");

    await user.keyboard("[Escape]");

    expect(rowsIn("In progress")).toEqual([
      expect.stringMatching(/Plan the week/),
    ]);
    expect(rowsIn("Next")).toEqual([expect.stringMatching(/Book travel/)]);
  });

  it("says where to drag an item when a group is empty", () => {
    renderList([item({ name: "Plan the week" })]);

    expect(
      screen.getByText("Drag an item here when you start it"),
    ).toBeInTheDocument();
  });

  it("keeps an item in its group when its status change is refused", async () => {
    const { onStatusChange } = renderList([
      item({ name: "Plan the week", status: "in_progress" }),
      item({ name: "Book travel" }),
    ]);
    onStatusChange.mockReturnValue(false);

    await dragWithKeyboard("Book travel", "[ArrowUp]");

    expect(rowsIn("Next")).toEqual([expect.stringMatching(/Book travel/)]);
  });

  it("shows the items given once a status change settles", async () => {
    const { onStatusChange } = renderList([
      item({ name: "Plan the week", status: "in_progress" }),
      item({ name: "Book travel" }),
    ]);
    await dragWithKeyboard("Book travel", "[ArrowUp]");
    const [[, , onSettled]] = onStatusChange.mock.calls;

    act(() => onSettled!());

    expect(rowsIn("Next")).toEqual([expect.stringMatching(/Book travel/)]);
  });

  it("keeps the items given when a move is refused", async () => {
    const { onMove } = renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    onMove.mockReturnValue(false);

    await dragWithKeyboard("Plan the week", "[ArrowDown]");

    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });

  it("shows the items given once a move settles", async () => {
    const { onMove } = renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    await dragWithKeyboard("Plan the week", "[ArrowDown]");
    const [[, onSettled]] = onMove.mock.calls;

    act(onSettled);

    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });
});
