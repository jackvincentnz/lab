import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { addSampleItems, sampleItems } from "./sampleItems.js";
import { itemStatuses } from "./schema.js";
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

const kept = sampleItems.filter((sample) => !sample.deleted);
const listed = kept.filter((sample) =>
  ["todo", "in_progress", "done"].includes(sample.status),
);

describe("addSampleItems", () => {
  it("lists the sample items on the main list, in order", () => {
    addSampleItems(store);

    expect(
      store.list().map(({ name, description, source, status }) => ({
        name,
        description,
        source,
        status,
      })),
    ).toEqual(
      listed.map(({ name, description = "", source = null, status }) => ({
        name,
        description,
        source,
        status,
      })),
    );
  });

  it.each(["deferred", "wont_do", "delegated"] as const)(
    "lists the %s sample items by their status",
    (status) => {
      addSampleItems(store);

      const names = store.list({ statuses: [status] }).map(({ name }) => name);

      expect(names).toEqual(
        kept
          .filter((sample) => sample.status === status)
          .map(({ name }) => name),
      );
    },
  );

  it("adds them below the items already listed", () => {
    const existing = store.add("Existing", "", null);

    addSampleItems(store);

    expect(store.list()[0]).toEqual(existing);
  });
});

describe("sampleItems", () => {
  it("covers every status", () => {
    const statuses = new Set(kept.map(({ status }) => status));

    expect([...statuses].sort()).toEqual([...itemStatuses].sort());
  });

  it("include a deleted item", () => {
    expect(sampleItems.some((sample) => sample.deleted)).toBe(true);
  });

  it("include a listed item with a source", () => {
    expect(listed.some((sample) => sample.source)).toBe(true);
  });
});
