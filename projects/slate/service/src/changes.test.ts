import { beforeEach, describe, expect, it, type Mock, vi } from "vitest";
import { announceChanges, ListChanges } from "./changes.js";
import { type Item, ItemChangedError } from "./item.js";
import { type Store } from "./operations.js";

let store: { [K in keyof Store]: Mock<Store[K]> };
let changes: ListChanges;
let changed: Mock<() => void>;
let announcing: Store;

beforeEach(() => {
  store = {
    list: vi.fn(),
    add: vi.fn(),
    update: vi.fn(),
    move: vi.fn(),
    delete: vi.fn(),
    restore: vi.fn(),
  };
  changes = new ListChanges();
  changed = vi.fn();
  changes.on("change", changed);
  announcing = announceChanges(store, changes);
});

function anItem(): Item {
  return {
    id: crypto.randomUUID(),
    name: "Write the plan",
    description: "",
    sources: [],
    status: "todo",
    rank: "a0",
    createdAt: "2026-11-22T09:00:00.000Z",
    updatedAt: "2026-11-22T09:00:00.000Z",
    doneAt: null,
  };
}

describe("announceChanges", () => {
  it("announces an added item after the store adds it", () => {
    const item = anItem();
    store.add.mockImplementation(() => {
      expect(changed).not.toHaveBeenCalled();
      return item;
    });

    const added = announcing.add(item.name, item.description, item.sources);

    expect(added).toBe(item);
    expect(store.add).toHaveBeenCalledWith(
      item.name,
      item.description,
      item.sources,
    );
    expect(changed).toHaveBeenCalledOnce();
  });

  it("announces an updated item", () => {
    const item = anItem();
    store.update.mockReturnValue(item);
    const input = {
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    } as const;

    const updated = announcing.update(input);

    expect(updated).toBe(item);
    expect(store.update).toHaveBeenCalledWith(input);
    expect(changed).toHaveBeenCalledOnce();
  });

  it("announces a moved item", () => {
    const item = anItem();
    store.move.mockReturnValue(item);
    const input = {
      id: item.id,
      neighbourId: crypto.randomUUID(),
      position: "before",
    } as const;

    const moved = announcing.move(input);

    expect(moved).toBe(item);
    expect(store.move).toHaveBeenCalledWith(input);
    expect(changed).toHaveBeenCalledOnce();
  });

  it("announces a deleted item", () => {
    const item = anItem();
    store.delete.mockReturnValue(item);

    const deleted = announcing.delete(item.id);

    expect(deleted).toBe(item);
    expect(store.delete).toHaveBeenCalledWith(item.id);
    expect(changed).toHaveBeenCalledOnce();
  });

  it("announces a restored item", () => {
    const item = anItem();
    store.restore.mockReturnValue(item);

    const restored = announcing.restore(item.id);

    expect(restored).toBe(item);
    expect(store.restore).toHaveBeenCalledWith(item.id);
    expect(changed).toHaveBeenCalledOnce();
  });

  it("lists the items without announcing a change", () => {
    const items = [anItem()];
    store.list.mockReturnValue(items);

    const query = { statuses: ["todo" as const] };

    const listed = announcing.list(query);

    expect(listed).toBe(items);
    expect(store.list).toHaveBeenCalledWith(query);
    expect(changed).not.toHaveBeenCalled();
  });

  it("announces nothing when the store refuses an update", () => {
    const item = anItem();
    store.update.mockImplementation(() => {
      throw new ItemChangedError(item.id);
    });

    expect(() =>
      announcing.update({ id: item.id, name: "Renamed", updatedAt: "" }),
    ).toThrow(ItemChangedError);
    expect(changed).not.toHaveBeenCalled();
  });
});
