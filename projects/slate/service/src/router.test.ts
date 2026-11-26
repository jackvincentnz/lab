import { beforeEach, describe, expect, it, type Mock, vi } from "vitest";
import { type Item, ItemChangedError } from "./item.js";
import { type Store } from "./operations.js";
import { slateRouter } from "./router.js";

let store: { [K in keyof Store]: Mock<Store[K]> };
let caller: ReturnType<typeof slateRouter.createCaller>;

beforeEach(() => {
  store = {
    list: vi.fn(),
    add: vi.fn(),
    update: vi.fn(),
    move: vi.fn(),
    delete: vi.fn(),
    restore: vi.fn(),
  };
  caller = slateRouter.createCaller({ store });
});

function anItem(): Item {
  return {
    id: crypto.randomUUID(),
    name: "Write the plan",
    description: "",
    status: "todo",
    rank: "a0",
    createdAt: "2026-11-22T09:00:00.000Z",
    updatedAt: "2026-11-22T09:00:00.000Z",
  };
}

describe("slateRouter", () => {
  it("lists the store's items", async () => {
    const items = [anItem(), anItem()];
    store.list.mockReturnValue(items);

    const listed = await caller.listItems();

    expect(listed).toEqual(items);
  });

  it("adds an item with its trimmed name", async () => {
    const item = anItem();
    store.add.mockReturnValue(item);

    const added = await caller.addItem({ name: "  Write the plan " });

    expect(store.add).toHaveBeenCalledWith("Write the plan", "");
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

    const updated = await caller.updateItem({ id: item.id, status: "done" });

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

  it("reports an unknown item on update", async () => {
    const updating = caller.updateItem({
      id: crypto.randomUUID(),
      status: "done",
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
});
