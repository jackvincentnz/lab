import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { addSampleItems, sampleItems } from "./sampleItems.js";
import { type ItemStore, openItemStore } from "./store.js";

let dataDir: string;
let store: ItemStore;

beforeEach(() => {
  dataDir = mkdtempSync(join(tmpdir(), "slate-samples-"));
  store = openItemStore(dataDir);
});

afterEach(() => {
  rmSync(dataDir, { recursive: true, force: true });
});

const listed = sampleItems.filter((sample) => !sample.deleted);

describe("addSampleItems", () => {
  it("lists the sample items that are not deleted, in order", () => {
    addSampleItems(store);

    expect(
      store.list().map(({ name, description, status }) => ({
        name,
        description,
        status,
      })),
    ).toEqual(
      listed.map(({ name, description = "", status }) => ({
        name,
        description,
        status,
      })),
    );
  });

  it("adds them below the items already listed", () => {
    const existing = store.add("Existing", "");

    addSampleItems(store);

    expect(store.list()[0]).toEqual(existing);
  });
});

describe("sampleItems", () => {
  it("covers every status", () => {
    const statuses = new Set(listed.map(({ status }) => status));

    expect([...statuses].sort()).toEqual(["doing", "done", "todo"]);
  });

  it("include a deleted item", () => {
    expect(sampleItems.some((sample) => sample.deleted)).toBe(true);
  });
});
