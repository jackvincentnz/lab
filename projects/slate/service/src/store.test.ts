import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { type Item, ItemChangedError } from "./item.js";
import { type ItemStore, openItemStore } from "./store.js";

let dataDir: string;
let time: Date;
let store: ItemStore;

beforeEach(() => {
  dataDir = mkdtempSync(join(tmpdir(), "slate-store-"));
  time = new Date("2026-11-21T09:00:00.000Z");
  store = openItemStore(dataDir, () => time);
});

afterEach(() => {
  rmSync(dataDir, { recursive: true, force: true });
});

function addItem(name: string): Item {
  return store.add(name, "");
}

function addItemsInListOrder(...itemNames: string[]): Item[] {
  return itemNames.toReversed().map(addItem).toReversed();
}

function names(items: Item[]): string[] {
  return items.map((item) => item.name);
}

describe("add", () => {
  it("creates a to-do item with matching timestamps", () => {
    const item = addItem("Write the plan");

    expect(item).toMatchObject({
      name: "Write the plan",
      status: "todo",
      createdAt: "2026-11-21T09:00:00.000Z",
      updatedAt: "2026-11-21T09:00:00.000Z",
    });
  });

  it("keeps the given description", () => {
    const item = store.add("Write the plan", "Why it matters");

    expect(item.description).toBe("Why it matters");
    expect(openItemStore(dataDir).list()).toEqual([item]);
  });

  it("puts the first item in an empty list", () => {
    const item = addItem("First");

    expect(store.list()).toEqual([item]);
  });

  it("puts each new item at the top", () => {
    addItem("First");
    addItem("Second");

    addItem("Third");

    expect(names(store.list())).toEqual(["Third", "Second", "First"]);
  });

  it("puts a new item above a deleted item that ranked first", () => {
    const [first] = addItemsInListOrder("A", "B");
    store.delete(first.id);

    addItem("C");

    expect(names(store.list())).toEqual(["C", "B"]);
  });
});

describe("list", () => {
  it("returns the items that the file already holds", () => {
    const item = addItem("Kept");

    const reopened = openItemStore(dataDir).list();

    expect(reopened).toEqual([item]);
  });

  it("leaves out deleted items", () => {
    const [first, second] = addItemsInListOrder("A", "B");
    store.delete(second.id);

    const reopened = openItemStore(dataDir).list();

    expect(reopened).toEqual([first]);
  });
});

describe("list on the day an item is marked done", () => {
  const lateEvening = new Date(2026, 10, 21, 23, 30);
  const lastMinute = new Date(2026, 10, 21, 23, 59, 59, 999);
  const midnight = new Date(2026, 10, 22);

  function markDone(name: string): Item {
    const added = addItem(name);
    time = lateEvening;
    return store.update({
      id: added.id,
      status: "done",
      updatedAt: added.updatedAt,
    })!;
  }

  it("keeps a done item until the end of that day", () => {
    const done = markDone("Ship it");

    time = lastMinute;

    expect(store.list()).toEqual([done]);
  });

  it("hides a done item from midnight", () => {
    markDone("Ship it");
    const todo = addItem("Next");

    time = midnight;

    expect(store.list()).toEqual([todo]);
  });

  it("keeps a done item hidden after it is renamed", () => {
    const done = markDone("Ship it");
    time = midnight;

    store.update({
      id: done.id,
      name: "Shipped",
      updatedAt: done.updatedAt,
    });

    expect(store.list()).toEqual([]);
  });

  it("keeps a done item hidden after it is moved", () => {
    const done = markDone("Ship it");
    const todo = addItem("Next");
    time = midnight;

    store.move({ id: done.id, neighbourId: todo.id, position: "before" });

    expect(store.list()).toEqual([todo]);
  });

  it("keeps a done item hidden when it is marked done again", () => {
    const done = markDone("Ship it");
    time = midnight;

    store.update({ id: done.id, status: "done", updatedAt: done.updatedAt });

    expect(store.list()).toEqual([]);
  });

  it("shows a hidden done item again when it is reopened", () => {
    const done = markDone("Ship it");
    time = midnight;

    const reopened = store.update({
      id: done.id,
      status: "todo",
      updatedAt: done.updatedAt,
    });

    expect(store.list()).toEqual([reopened]);
  });

  it("shows a reopened item on the day it is marked done again", () => {
    const done = markDone("Ship it");
    time = midnight;
    const doing = store.update({
      id: done.id,
      status: "doing",
      updatedAt: done.updatedAt,
    })!;

    const doneAgain = store.update({
      id: done.id,
      status: "done",
      updatedAt: doing.updatedAt,
    });

    expect(store.list()).toEqual([doneAgain]);
  });

  it("keeps a doing item after midnight", () => {
    const added = addItem("Ongoing");
    time = lateEvening;
    const doing = store.update({
      id: added.id,
      status: "doing",
      updatedAt: added.updatedAt,
    });

    time = midnight;

    expect(store.list()).toEqual([doing]);
  });
});

describe("update", () => {
  it("changes only the given fields and the updated time", () => {
    const item = addItem("Draft");
    time = new Date("2026-11-21T10:00:00.000Z");

    const updated = store.update({
      id: item.id,
      status: "doing",
      updatedAt: item.updatedAt,
    });

    expect(updated).toEqual({
      ...item,
      status: "doing",
      updatedAt: "2026-11-21T10:00:00.000Z",
    });
    expect(store.list()).toEqual([updated]);
  });

  it("renames an item", () => {
    const item = addItem("Draft");

    const updated = store.update({
      id: item.id,
      name: "Final",
      updatedAt: item.updatedAt,
    });

    expect(updated?.name).toBe("Final");
  });

  it("replaces an item's description", () => {
    const item = store.add("Draft", "Old notes");

    const updated = store.update({
      id: item.id,
      description: "New notes",
      updatedAt: item.updatedAt,
    });

    expect(updated?.description).toBe("New notes");
    expect(openItemStore(dataDir).list()).toEqual([updated]);
  });

  it("returns nothing for an unknown item", () => {
    const updated = store.update({
      id: crypto.randomUUID(),
      name: "Missing",
      updatedAt: "2026-11-21T09:00:00.000Z",
    });

    expect(updated).toBeUndefined();
  });

  it("returns nothing for a deleted item", () => {
    const item = addItem("Draft");
    store.delete(item.id);

    const updated = store.update({
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    });

    expect(updated).toBeUndefined();
  });

  it("refuses an update made from an older copy of the item", () => {
    const item = addItem("Draft");
    time = new Date("2026-11-21T10:00:00.000Z");
    const renamed = store.update({
      id: item.id,
      name: "Agent's name",
      updatedAt: item.updatedAt,
    });

    const updating = () =>
      store.update({ id: item.id, name: "Mine", updatedAt: item.updatedAt });

    expect(updating).toThrow(ItemChangedError);
    expect(store.list()).toEqual([renamed]);
  });

  it("returns nothing for an item deleted since it was read", () => {
    const item = addItem("Draft");
    time = new Date("2026-11-21T10:00:00.000Z");
    store.delete(item.id);

    const updated = store.update({
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    });

    expect(updated).toBeUndefined();
  });
});

describe("move", () => {
  it("places an item before its neighbour", () => {
    const [first, , third] = addItemsInListOrder("A", "B", "C");

    store.move({ id: third.id, neighbourId: first.id, position: "before" });

    expect(names(store.list())).toEqual(["C", "A", "B"]);
  });

  it("places an item after its neighbour", () => {
    const [first, second] = addItemsInListOrder("A", "B", "C");

    store.move({ id: first.id, neighbourId: second.id, position: "after" });

    expect(names(store.list())).toEqual(["B", "A", "C"]);
  });

  it("places an item between its neighbour and the next item", () => {
    const [first, second, , fourth] = addItemsInListOrder("A", "B", "C", "D");

    store.move({ id: fourth.id, neighbourId: first.id, position: "after" });
    store.move({ id: first.id, neighbourId: second.id, position: "before" });

    expect(names(store.list())).toEqual(["D", "A", "B", "C"]);
  });

  it("changes only the moved item's rank and updated time", () => {
    const [first, second, third] = addItemsInListOrder("A", "B", "C");
    time = new Date("2026-11-21T10:00:00.000Z");

    const moved = store.move({
      id: third.id,
      neighbourId: second.id,
      position: "before",
    });

    expect(moved?.updatedAt).toBe("2026-11-21T10:00:00.000Z");
    expect(store.list()).toEqual([first, moved, second]);
  });

  it("leaves an item moved next to itself unchanged", () => {
    const [first, second] = addItemsInListOrder("A", "B");
    time = new Date("2026-11-21T10:00:00.000Z");

    const moved = store.move({
      id: second.id,
      neighbourId: second.id,
      position: "before",
    });

    expect(moved).toEqual(second);
    expect(store.list()).toEqual([first, second]);
  });

  it("returns nothing when the neighbour is unknown", () => {
    const item = addItem("Lonely");

    const moved = store.move({
      id: item.id,
      neighbourId: crypto.randomUUID(),
      position: "after",
    });

    expect(moved).toBeUndefined();
  });

  it("returns nothing for a deleted item", () => {
    const [first, second] = addItemsInListOrder("A", "B");
    store.delete(second.id);

    const moved = store.move({
      id: second.id,
      neighbourId: first.id,
      position: "before",
    });

    expect(moved).toBeUndefined();
  });

  it("returns nothing when the neighbour is deleted", () => {
    const [first, second] = addItemsInListOrder("A", "B");
    store.delete(first.id);

    const moved = store.move({
      id: second.id,
      neighbourId: first.id,
      position: "before",
    });

    expect(moved).toBeUndefined();
    expect(store.list()).toEqual([second]);
  });
});

describe("delete", () => {
  it("returns the deleted item with its update time", () => {
    const item = addItem("Mistake");
    time = new Date("2026-11-21T10:00:00.000Z");

    const deleted = store.delete(item.id);

    expect(deleted).toEqual({ ...item, updatedAt: "2026-11-21T10:00:00.000Z" });
    expect(store.list()).toEqual([]);
  });

  it("returns nothing for an unknown item", () => {
    const deleted = store.delete(crypto.randomUUID());

    expect(deleted).toBeUndefined();
  });

  it("returns nothing for an item already deleted", () => {
    const item = addItem("Mistake");
    store.delete(item.id);

    const deletedAgain = store.delete(item.id);

    expect(deletedAgain).toBeUndefined();
  });
});

describe("restore", () => {
  it("puts a deleted item back in its place with its update time", () => {
    const [first, second, third] = addItemsInListOrder("A", "B", "C");
    store.delete(second.id);
    time = new Date("2026-11-21T10:00:00.000Z");

    const restored = store.restore(second.id);

    const expected = { ...second, updatedAt: "2026-11-21T10:00:00.000Z" };
    expect(restored).toEqual(expected);
    expect(store.list()).toEqual([first, expected, third]);
  });

  it("returns an item that is not deleted unchanged", () => {
    const item = addItem("Kept");
    time = new Date("2026-11-21T10:00:00.000Z");

    const restored = store.restore(item.id);

    expect(restored).toEqual(item);
    expect(store.list()).toEqual([item]);
  });

  it("returns nothing for an unknown item", () => {
    const restored = store.restore(crypto.randomUUID());

    expect(restored).toBeUndefined();
  });

  it("keeps an item added after a deletion above the restored item", () => {
    const [first] = addItemsInListOrder("A", "B");
    store.delete(first.id);
    addItem("C");

    store.restore(first.id);

    expect(names(store.list())).toEqual(["C", "A", "B"]);
  });

  it("puts an item back beside one moved into its place", () => {
    const [first, second, third] = addItemsInListOrder("A", "B", "C");
    store.delete(second.id);
    store.move({ id: third.id, neighbourId: first.id, position: "after" });

    store.restore(second.id);

    expect(names(store.list())).toEqual(["A", "C", "B"]);
  });
});
