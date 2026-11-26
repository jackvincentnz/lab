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

function names(items: Item[]): string[] {
  return items.map((item) => item.name);
}

describe("add", () => {
  it("creates a to-do item with matching timestamps", () => {
    const item = store.add("Write the plan");

    expect(item).toMatchObject({
      name: "Write the plan",
      description: "",
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

  it("puts each new item at the bottom", () => {
    store.add("First");
    store.add("Second");

    store.add("Third");

    expect(names(store.list())).toEqual(["First", "Second", "Third"]);
  });
});

describe("list", () => {
  it("returns the items that the file already holds", () => {
    const item = store.add("Kept");

    const reopened = openItemStore(dataDir).list();

    expect(reopened).toEqual([item]);
  });

  it("leaves out deleted items", () => {
    const [first, second] = ["A", "B"].map((name) => store.add(name));
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
    const added = store.add(name);
    time = lateEvening;
    return store.update({ id: added.id, status: "done" })!;
  }

  it("keeps a done item until the end of that day", () => {
    const done = markDone("Ship it");

    time = lastMinute;

    expect(store.list()).toEqual([done]);
  });

  it("hides a done item from midnight", () => {
    markDone("Ship it");
    const todo = store.add("Next");

    time = midnight;

    expect(store.list()).toEqual([todo]);
  });

  it("keeps a done item hidden after it is renamed", () => {
    const done = markDone("Ship it");
    time = midnight;

    store.update({ id: done.id, name: "Shipped" });

    expect(store.list()).toEqual([]);
  });

  it("keeps a done item hidden after it is moved", () => {
    const done = markDone("Ship it");
    const todo = store.add("Next");
    time = midnight;

    store.move({ id: done.id, neighbourId: todo.id, position: "before" });

    expect(store.list()).toEqual([todo]);
  });

  it("keeps a done item hidden when it is marked done again", () => {
    const done = markDone("Ship it");
    time = midnight;

    store.update({ id: done.id, status: "done" });

    expect(store.list()).toEqual([]);
  });

  it("shows a hidden done item again when it is reopened", () => {
    const done = markDone("Ship it");
    time = midnight;

    const reopened = store.update({ id: done.id, status: "todo" });

    expect(store.list()).toEqual([reopened]);
  });

  it("shows a reopened item on the day it is marked done again", () => {
    const done = markDone("Ship it");
    time = midnight;
    store.update({ id: done.id, status: "doing" });

    const doneAgain = store.update({ id: done.id, status: "done" });

    expect(store.list()).toEqual([doneAgain]);
  });

  it("keeps a doing item after midnight", () => {
    const added = store.add("Ongoing");
    time = lateEvening;
    const doing = store.update({ id: added.id, status: "doing" });

    time = midnight;

    expect(store.list()).toEqual([doing]);
  });
});

describe("update", () => {
  it("changes only the given fields and the updated time", () => {
    const item = store.add("Draft");
    time = new Date("2026-11-21T10:00:00.000Z");

    const updated = store.update({ id: item.id, status: "doing" });

    expect(updated).toEqual({
      ...item,
      status: "doing",
      updatedAt: "2026-11-21T10:00:00.000Z",
    });
    expect(store.list()).toEqual([updated]);
  });

  it("renames an item", () => {
    const item = store.add("Draft");

    const updated = store.update({ id: item.id, name: "Final" });

    expect(updated?.name).toBe("Final");
  });

  it("replaces an item's description", () => {
    const item = store.add("Draft", "Old notes");

    const updated = store.update({ id: item.id, description: "New notes" });

    expect(updated?.description).toBe("New notes");
    expect(openItemStore(dataDir).list()).toEqual([updated]);
  });

  it("returns nothing for an unknown item", () => {
    const updated = store.update({ id: crypto.randomUUID(), name: "Missing" });

    expect(updated).toBeUndefined();
  });

  it("returns nothing for a deleted item", () => {
    const item = store.add("Draft");
    store.delete(item.id);

    const updated = store.update({ id: item.id, status: "done" });

    expect(updated).toBeUndefined();
  });

  it("updates an item that has not changed since it was read", () => {
    const item = store.add("Draft");

    const updated = store.update({
      id: item.id,
      name: "Final",
      updatedAt: item.updatedAt,
    });

    expect(updated?.name).toBe("Final");
  });

  it("refuses an update made from an older copy of the item", () => {
    const item = store.add("Draft");
    time = new Date("2026-11-21T10:00:00.000Z");
    const renamed = store.update({ id: item.id, name: "Agent's name" });

    const updating = () =>
      store.update({ id: item.id, name: "Mine", updatedAt: item.updatedAt });

    expect(updating).toThrow(ItemChangedError);
    expect(store.list()).toEqual([renamed]);
  });

  it("returns nothing for an item deleted since it was read", () => {
    const item = store.add("Draft");
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
    const [first, , third] = ["A", "B", "C"].map((name) => store.add(name));

    store.move({ id: third.id, neighbourId: first.id, position: "before" });

    expect(names(store.list())).toEqual(["C", "A", "B"]);
  });

  it("places an item after its neighbour", () => {
    const [first, second] = ["A", "B", "C"].map((name) => store.add(name));

    store.move({ id: first.id, neighbourId: second.id, position: "after" });

    expect(names(store.list())).toEqual(["B", "A", "C"]);
  });

  it("places an item between its neighbour and the next item", () => {
    const [first, second, , fourth] = ["A", "B", "C", "D"].map((name) =>
      store.add(name),
    );

    store.move({ id: fourth.id, neighbourId: first.id, position: "after" });
    store.move({ id: first.id, neighbourId: second.id, position: "before" });

    expect(names(store.list())).toEqual(["D", "A", "B", "C"]);
  });

  it("changes only the moved item's rank and updated time", () => {
    const [first, second, third] = ["A", "B", "C"].map((name) =>
      store.add(name),
    );
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
    const [first, second] = ["A", "B"].map((name) => store.add(name));
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
    const item = store.add("Lonely");

    const moved = store.move({
      id: item.id,
      neighbourId: crypto.randomUUID(),
      position: "after",
    });

    expect(moved).toBeUndefined();
  });

  it("returns nothing for a deleted item", () => {
    const [first, second] = ["A", "B"].map((name) => store.add(name));
    store.delete(second.id);

    const moved = store.move({
      id: second.id,
      neighbourId: first.id,
      position: "before",
    });

    expect(moved).toBeUndefined();
  });

  it("returns nothing when the neighbour is deleted", () => {
    const [first, second] = ["A", "B"].map((name) => store.add(name));
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
    const item = store.add("Mistake");
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
    const item = store.add("Mistake");
    store.delete(item.id);

    const deletedAgain = store.delete(item.id);

    expect(deletedAgain).toBeUndefined();
  });
});

describe("restore", () => {
  it("puts a deleted item back in its place with its update time", () => {
    const [first, second, third] = ["A", "B", "C"].map((name) =>
      store.add(name),
    );
    store.delete(second.id);
    time = new Date("2026-11-21T10:00:00.000Z");

    const restored = store.restore(second.id);

    const expected = { ...second, updatedAt: "2026-11-21T10:00:00.000Z" };
    expect(restored).toEqual(expected);
    expect(store.list()).toEqual([first, expected, third]);
  });

  it("returns an item that is not deleted unchanged", () => {
    const item = store.add("Kept");
    time = new Date("2026-11-21T10:00:00.000Z");

    const restored = store.restore(item.id);

    expect(restored).toEqual(item);
    expect(store.list()).toEqual([item]);
  });

  it("returns nothing for an unknown item", () => {
    const restored = store.restore(crypto.randomUUID());

    expect(restored).toBeUndefined();
  });

  it("keeps an item added after a deletion below the restored item", () => {
    const [, second] = ["A", "B"].map((name) => store.add(name));
    store.delete(second.id);
    store.add("C");

    store.restore(second.id);

    expect(names(store.list())).toEqual(["A", "B", "C"]);
  });

  it("puts an item back beside one moved into its place", () => {
    const [first, second, third] = ["A", "B", "C"].map((name) =>
      store.add(name),
    );
    store.delete(second.id);
    store.move({ id: third.id, neighbourId: first.id, position: "after" });

    store.restore(second.id);

    expect(names(store.list())).toEqual(["A", "C", "B"]);
  });
});
