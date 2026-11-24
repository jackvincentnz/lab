import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { type Item, type ItemStore, openItemStore } from "./store.js";

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
      status: "todo",
      createdAt: "2026-11-21T09:00:00.000Z",
      updatedAt: "2026-11-21T09:00:00.000Z",
    });
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

  it("returns nothing for an unknown item", () => {
    const updated = store.update({ id: crypto.randomUUID(), name: "Missing" });

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
});
