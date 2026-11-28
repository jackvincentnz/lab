import { beforeEach, describe, expect, it, type Mock, vi } from "vitest";
import { ListChanges } from "./changes.js";
import { type Item, ItemChangedError, SourceChangeError } from "./item.js";
import { type Store } from "./operations.js";
import { slateRouter } from "./router.js";

let store: { [K in keyof Store]: Mock<Store[K]> };
let changes: ListChanges;
let caller: ReturnType<typeof slateRouter.createCaller>;

beforeEach(() => {
  store = {
    list: vi.fn(),
    add: vi.fn(),
    update: vi.fn(),
    move: vi.fn(),
    delete: vi.fn(),
    restore: vi.fn(),
    changes: vi.fn(),
    link: vi.fn(),
    unlink: vi.fn(),
    linked: vi.fn(),
  };
  changes = new ListChanges();
  caller = slateRouter.createCaller({ store, changes });
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

describe("slateRouter", () => {
  it("lists the store's items", async () => {
    const items = [anItem(), anItem()];
    store.list.mockReturnValue(items);

    const listed = await caller.listItems();

    expect(listed).toEqual(items);
  });

  it("lists every item that matches the given query", async () => {
    const items = [anItem()];
    store.list.mockReturnValue(items);

    const listed = await caller.listItems({
      statuses: ["done"],
      doneTodayOnly: false,
      deleted: true,
      source: "https://example.com/pull/1",
      search: "plan",
      orderBy: "status",
    });

    expect(store.list).toHaveBeenCalledWith({
      statuses: ["done"],
      doneTodayOnly: false,
      deleted: true,
      source: "https://example.com/pull/1",
      search: "plan",
      orderBy: "status",
    });
    expect(listed).toEqual(items);
  });

  it("adds an item with its trimmed name", async () => {
    const item = anItem();
    store.add.mockReturnValue(item);

    const added = await caller.addItem({ name: "  Write the plan " });

    expect(store.add).toHaveBeenCalledWith("Write the plan", "", []);
    expect(added).toEqual(item);
  });

  it("rejects invalid input before reaching the store", async () => {
    const adding = caller.addItem({ name: "" });

    await expect(adding).rejects.toMatchObject({ code: "BAD_REQUEST" });
    expect(store.add).not.toHaveBeenCalled();
  });

  it("returns the updated item", async () => {
    const item = anItem();
    store.update.mockReturnValue(item);

    const updated = await caller.updateItem({
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    });

    expect(updated).toEqual(item);
  });

  it("reports a conflict when the item changed since it was read", async () => {
    const item = anItem();
    store.update.mockImplementation(() => {
      throw new ItemChangedError(item.id);
    });

    const updating = caller.updateItem({
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    });

    await expect(updating).rejects.toMatchObject({ code: "CONFLICT" });
  });

  it("rejects replacing a source the item does not have", async () => {
    const item = anItem();
    store.update.mockImplementation(() => {
      throw new SourceChangeError("The item has no source with that id");
    });

    const updating = caller.updateItem({
      id: item.id,
      replaceSource: {
        id: crypto.randomUUID(),
        to: { kind: "reference", text: "notes/plan.md", label: "Plan" },
      },
      updatedAt: item.updatedAt,
    });

    await expect(updating).rejects.toMatchObject({ code: "BAD_REQUEST" });
  });

  it("reports an unknown item on update", async () => {
    const updating = caller.updateItem({
      id: crypto.randomUUID(),
      status: "done",
      updatedAt: "2026-11-22T09:00:00.000Z",
    });

    await expect(updating).rejects.toMatchObject({ code: "NOT_FOUND" });
  });

  it("returns the moved item", async () => {
    const item = anItem();
    store.move.mockReturnValue(item);

    const moved = await caller.moveItem({
      id: item.id,
      neighbourId: crypto.randomUUID(),
      position: "after",
    });

    expect(moved).toEqual(item);
  });

  it("reports an unknown item on move", async () => {
    const moving = caller.moveItem({
      id: crypto.randomUUID(),
      neighbourId: crypto.randomUUID(),
      position: "before",
    });

    await expect(moving).rejects.toMatchObject({ code: "NOT_FOUND" });
  });

  it("returns the deleted item", async () => {
    const item = anItem();
    store.delete.mockReturnValue(item);

    const deleted = await caller.deleteItem({ id: item.id });

    expect(store.delete).toHaveBeenCalledWith(item.id);
    expect(deleted).toEqual(item);
  });

  it("reports an unknown item on delete", async () => {
    const deleting = caller.deleteItem({ id: crypto.randomUUID() });

    await expect(deleting).rejects.toMatchObject({ code: "NOT_FOUND" });
  });

  it("returns the restored item", async () => {
    const item = anItem();
    store.restore.mockReturnValue(item);

    const restored = await caller.restoreItem({ id: item.id });

    expect(store.restore).toHaveBeenCalledWith(item.id);
    expect(restored).toEqual(item);
  });

  it("reports an unknown item on restore", async () => {
    const restoring = caller.restoreItem({ id: crypto.randomUUID() });

    await expect(restoring).rejects.toMatchObject({ code: "NOT_FOUND" });
  });

  it("lists an item's changes", async () => {
    const id = crypto.randomUUID();
    const changes = [
      {
        at: "2026-11-22T09:00:00.000Z",
        actor: "agent" as const,
        kind: "rank" as const,
        status: null,
      },
    ];
    store.changes.mockReturnValue(changes);

    const listed = await caller.listItemChanges({ id });

    expect(store.changes).toHaveBeenCalledWith(id);
    expect(listed).toEqual(changes);
  });

  it("reports an unknown item when listing changes", async () => {
    await expect(
      caller.listItemChanges({ id: crypto.randomUUID() }),
    ).rejects.toMatchObject({ code: "NOT_FOUND" });
  });

  it("links two items", async () => {
    const item = anItem();
    const linkedId = crypto.randomUUID();
    store.link.mockReturnValue(item);

    const linked = await caller.linkItems({ id: item.id, linkedId });

    expect(store.link).toHaveBeenCalledWith({ id: item.id, linkedId });
    expect(linked).toEqual(item);
  });

  it("reports an unknown item on link", async () => {
    await expect(
      caller.linkItems({
        id: crypto.randomUUID(),
        linkedId: crypto.randomUUID(),
      }),
    ).rejects.toMatchObject({ code: "NOT_FOUND" });
  });

  it("unlinks two items", async () => {
    const item = anItem();
    const linkedId = crypto.randomUUID();
    store.unlink.mockReturnValue(item);

    const unlinked = await caller.unlinkItems({ id: item.id, linkedId });

    expect(store.unlink).toHaveBeenCalledWith({ id: item.id, linkedId });
    expect(unlinked).toEqual(item);
  });

  it("lists an item's linked items", async () => {
    const id = crypto.randomUUID();
    const items = [anItem()];
    store.linked.mockReturnValue(items);

    const listed = await caller.listLinkedItems({ id });

    expect(store.linked).toHaveBeenCalledWith(id);
    expect(listed).toEqual(items);
  });

  it("tells a subscriber each time the list changes", async () => {
    const subscription = await caller.listChanged();
    const next = () => subscription[Symbol.asyncIterator]().next();

    const first = next();
    changes.emit("change");
    const second = next();
    changes.emit("change");

    await expect(first).resolves.toEqual({ value: null, done: false });
    await expect(second).resolves.toEqual({ value: null, done: false });
  });

  it("stops listening for changes when the subscriber leaves", async () => {
    const leaving = new AbortController();
    const subscription = await slateRouter
      .createCaller({ store, changes }, { signal: leaving.signal })
      .listChanged();
    const next = subscription[Symbol.asyncIterator]().next();

    leaving.abort();

    await expect(next).rejects.toThrow();
    expect(changes.listenerCount("change")).toBe(0);
  });

  it("stops listening for changes when the subscriber stops reading", async () => {
    const subscription = await caller.listChanged();
    const reading = subscription[Symbol.asyncIterator]();
    const next = reading.next();
    changes.emit("change");
    await next;

    await reading.return?.();

    expect(changes.listenerCount("change")).toBe(0);
  });
});
