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
import { item } from "../test/items";
import { layOutRowsVertically } from "../test/layout";
import { ItemList } from "./ItemList";

type ItemListProps = Parameters<typeof ItemList>[0];

function renderList(items: Item[]) {
  const onMove = vi.fn<ItemListProps["onMove"]>();
  render(
    <ItemList
      items={items}
      saving={false}
      onMove={onMove}
      onRename={vi.fn()}
      onRenameCancel={vi.fn()}
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
        saving={false}
        onMove={vi.fn()}
        onRename={vi.fn()}
        onRenameCancel={vi.fn()}
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

  it("reports which item a change is for", async () => {
    const user = userEvent.setup();
    const bookTravel = item({ name: "Book travel" });
    const onStatusChange = vi.fn<ItemListProps["onStatusChange"]>();
    const onRename = vi.fn<ItemListProps["onRename"]>();
    render(
      <ItemList
        items={[item({ name: "Plan the week" }), bookTravel]}
        saving={false}
        onMove={vi.fn()}
        onRename={onRename}
        onRenameCancel={vi.fn()}
        onStatusChange={onStatusChange}
      />,
    );

    await user.selectOptions(
      screen.getByRole("combobox", { name: "Status of Book travel" }),
      "Done",
    );
    await user.click(
      screen.getByRole("button", { name: "Rename Book travel" }),
    );
    await user.type(screen.getByRole("textbox", { name: "Name" }), "s{Enter}");

    expect(onStatusChange).toHaveBeenCalledWith(bookTravel, "done");
    expect(onRename).toHaveBeenCalledWith(
      bookTravel,
      "Book travels",
      expect.any(Function),
    );
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
