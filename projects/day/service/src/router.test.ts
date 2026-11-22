import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { dayRouter } from "./router.js";
import { openItemStore } from "./store.js";

let dataDir: string;
let caller: ReturnType<typeof dayRouter.createCaller>;

beforeEach(() => {
  dataDir = mkdtempSync(join(tmpdir(), "day-router-"));
  caller = dayRouter.createCaller({ store: openItemStore(dataDir) });
});

afterEach(() => {
  rmSync(dataDir, { recursive: true, force: true });
});

describe("dayRouter", () => {
  it("lists added items in order", async () => {
    const first = await caller.addItem({ name: "First" });
    const second = await caller.addItem({ name: "Second" });

    const items = await caller.listItems();

    expect(items).toEqual([first, second]);
  });

  it("rejects invalid input", async () => {
    const adding = caller.addItem({ name: "" });

    await expect(adding).rejects.toMatchObject({ code: "BAD_REQUEST" });
  });

  it("reports an unknown item on update", async () => {
    const updating = caller.updateItem({
      id: crypto.randomUUID(),
      status: "done",
    });

    await expect(updating).rejects.toMatchObject({ code: "NOT_FOUND" });
  });

  it("reports an unknown item on move", async () => {
    const neighbour = await caller.addItem({ name: "Neighbour" });

    const moving = caller.moveItem({
      id: crypto.randomUUID(),
      neighbourId: neighbour.id,
      position: "before",
    });

    await expect(moving).rejects.toMatchObject({ code: "NOT_FOUND" });
  });
});
