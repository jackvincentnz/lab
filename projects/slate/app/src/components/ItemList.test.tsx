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
  render(
    <ItemList
      items={items}
      disabled={false}
      openId={undefined}
      onMove={onMove}
      onOpen={vi.fn()}
      onStatusChange={vi.fn()}
    />,
  );
  return onMove;
}

function itemNames() {
  return screen
    .getAllByRole("button", { name: /^Move / })
    .map((handle) => handle.getAttribute("aria-label")!.slice("Move ".length));
}

function ranks() {
  return screen.getAllByText(/^#\d+$/).map((rank) => rank.textContent);
}

async function dragWithKeyboard(name: string, keys: string) {
  const user = userEvent.setup();
  layOutRowsVertically();
  screen.getByRole("button", { name: `Move ${name}` }).focus();
  await user.keyboard(`[Space]${keys}[Space]`);
}

describe("ItemList", () => {
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

    const rows = within(
      screen.getByRole("list", { name: "Items" }),
    ).getAllByRole("listitem");
    expect(rows.map((row) => row.textContent)).toEqual([
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
    const onMove = renderList([planTheWeek, bookTravel, item()]);

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
    const onMove = renderList([planTheWeek, bookTravel]);

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
    const onMove = renderList([
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

  it("keeps the items given when a move is refused", async () => {
    const onMove = renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    onMove.mockReturnValue(false);

    await dragWithKeyboard("Plan the week", "[ArrowDown]");

    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });

  it("shows the items given once a move settles", async () => {
    const onMove = renderList([
      item({ name: "Plan the week" }),
      item({ name: "Book travel" }),
    ]);
    await dragWithKeyboard("Plan the week", "[ArrowDown]");
    const [[, onSettled]] = onMove.mock.calls;

    act(onSettled);

    expect(itemNames()).toEqual(["Plan the week", "Book travel"]);
  });
});
