import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import {
  type Item,
  ItemChangedError,
  SourceChangeError,
  sourceKey,
  type ItemSource,
  type ItemStatus,
  itemStatus,
  type NewSource,
} from "./item.js";
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
  return store.add(name, "", []);
}

function withStatus(item: Item, status: ItemStatus): Item {
  return store.update({ id: item.id, status, updatedAt: item.updatedAt })!;
}

const everyStatus = itemStatus.options;

function aReference(): NewSource {
  return {
    kind: "reference",
    text: `notes/${crypto.randomUUID()}.md:12`,
    label: `Notes ${crypto.randomUUID()}`,
  };
}

type LinkSource = Extract<NewSource, { kind: "link" }>;

function aSource(): LinkSource {
  return {
    kind: "link",
    url: `https://example.com/${crypto.randomUUID()}`,
    label: `Doc ${crypto.randomUUID()}`,
  };
}

function withoutIds(sources: ItemSource[] | undefined): NewSource[] {
  return (sources ?? []).map(newSourceOf);
}

function newSourceOf(source: ItemSource): NewSource {
  return source.kind === "link"
    ? { kind: "link", url: source.url, label: source.label }
    : { kind: "reference", text: source.text, label: source.label };
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
    const item = store.add("Write the plan", "Why it matters", []);

    expect(item.description).toBe("Why it matters");
    expect(openItemStore(dataDir).list()).toEqual([item]);
  });

  it("keeps the given sources in order", () => {
    const sources = [aSource(), aReference(), aSource()];

    const item = store.add("Write the plan", "", sources);

    expect(withoutIds(item.sources)).toEqual(sources);
    expect(openItemStore(dataDir).list()).toEqual([item]);
  });

  it("gives each source its own id", () => {
    const item = store.add("Write the plan", "", [aSource(), aReference()]);

    const [first, second] = item.sources;

    expect(first?.id).toEqual(expect.any(String));
    expect(second?.id).toEqual(expect.any(String));
    expect(first?.id).not.toBe(second?.id);
  });

  it("keeps the first of two sources with the same url or text", () => {
    const first = aSource();
    const reference = aReference();

    const item = store.add("Write the plan", "", [
      first,
      reference,
      { ...first, label: "Again" },
      { ...reference, label: "Again" },
    ]);

    expect(withoutIds(item.sources)).toEqual([first, reference]);
  });

  it("adds an item without sources", () => {
    addItem("Write the plan");

    expect(openItemStore(dataDir).list()[0]?.sources).toEqual([]);
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
    const [, b] = ["A", "B"].map(addItem);
    store.delete(b.id);

    addItem("C");

    expect(names(store.list())).toEqual(["C", "A"]);
  });
});

describe("list", () => {
  it("returns the items that the file already holds", () => {
    const item = addItem("Kept");

    const reopened = openItemStore(dataDir).list();

    expect(reopened).toEqual([item]);
  });

  it("leaves out deleted items", () => {
    const [a, b] = ["A", "B"].map(addItem);
    store.delete(b.id);

    const reopened = openItemStore(dataDir).list();

    expect(reopened).toEqual([a]);
  });
});

describe.each(["deferred", "wont_do", "delegated"] as const)(
  "list with an item that is %s",
  (status) => {
    function setStatus(item: Item, to: ItemStatus): Item {
      return store.update({
        id: item.id,
        status: to,
        updatedAt: item.updatedAt,
      })!;
    }

    it("leaves the item out of the main list", () => {
      const [a, b] = ["A", "B"].map(addItem);

      setStatus(b, status);

      expect(store.list()).toEqual([a]);
    });

    it("lists the item by its status", () => {
      const [a] = ["A", "B"].map(addItem);

      const setAside = setStatus(a, status);

      expect(store.list({ statuses: [status] })).toEqual([setAside]);
    });

    it("puts the item back in its place when it is reopened", () => {
      const [a, b, c] = ["A", "B", "C"].map(addItem);
      const setAside = setStatus(b, status);

      const reopened = setStatus(setAside, "todo");

      expect(store.list()).toEqual([c, reopened, a]);
      expect(reopened.rank).toBe(b.rank);
    });
  },
);

describe("list by status", () => {
  it("returns the items with the given statuses in priority order", () => {
    const [a, b, c] = ["A", "B", "C"].map(addItem);
    const inProgressA = store.update({
      id: a.id,
      status: "in_progress",
      updatedAt: a.updatedAt,
    });
    const deferredB = store.update({
      id: b.id,
      status: "deferred",
      updatedAt: b.updatedAt,
    });
    store.update({ id: c.id, status: "wont_do", updatedAt: c.updatedAt });

    const listed = store.list({ statuses: ["in_progress", "deferred"] });

    expect(listed).toEqual([deferredB, inProgressA]);
  });

  it("leaves out items with another status", () => {
    const [a, b] = ["A", "B"].map(addItem);
    store.update({ id: a.id, status: "in_progress", updatedAt: a.updatedAt });

    const todo = store.list({ statuses: ["todo"] });

    expect(todo).toEqual([b]);
  });

  it("leaves out done items marked done before today", () => {
    const added = addItem("Shipped");
    store.update({ id: added.id, status: "done", updatedAt: added.updatedAt });
    time = new Date(time.getTime() + 2 * 24 * 60 * 60 * 1000);

    const listed = store.list({ statuses: ["done"] });

    expect(listed).toEqual([]);
  });

  it("includes done items marked done before today when not only today's", () => {
    const added = addItem("Shipped");
    const done = store.update({
      id: added.id,
      status: "done",
      updatedAt: added.updatedAt,
    });
    time = new Date(time.getTime() + 2 * 24 * 60 * 60 * 1000);

    const listed = store.list({ statuses: ["done"], doneTodayOnly: false });

    expect(listed).toEqual([done]);
  });

  it("leaves out deleted items", () => {
    const added = addItem("Mistake");
    const deferred = store.update({
      id: added.id,
      status: "deferred",
      updatedAt: added.updatedAt,
    })!;
    store.delete(deferred.id);

    const listed = store.list({ statuses: ["deferred"] });

    expect(listed).toEqual([]);
  });
});

describe("list by source", () => {
  it("lists only the items with a link of exactly that url", () => {
    const link = aSource();
    const tracked = store.add("Tracked", "", [aReference(), link]);
    store.add("Other", "", [aSource()]);
    store.add("Close", "", [{ ...link, url: `${link.url}/more` }]);

    const listed = store.list({ source: sourceKey(link) });

    expect(listed).toEqual([tracked]);
  });

  it("lists only the items with a reference of exactly that text", () => {
    const reference = aReference();
    const tracked = store.add("Tracked", "", [reference]);
    store.add("Other", "", [aReference()]);

    const listed = store.list({ source: sourceKey(reference) });

    expect(listed).toEqual([tracked]);
  });

  it("combines with the other filters", () => {
    const link = aSource();
    const todo = store.add("Todo", "", [link]);
    const deferred = store.add("Deferred", "", [link]);
    store.update({
      id: deferred.id,
      status: "deferred",
      updatedAt: deferred.updatedAt,
    });

    const listed = store.list({ source: sourceKey(link) });

    expect(listed).toEqual([todo]);
  });
});

describe("list in order of a time", () => {
  it("returns done items most recently done first", () => {
    const [a, b] = ["A", "B"].map(addItem);
    const doneB = store.update({
      id: b.id,
      status: "done",
      updatedAt: b.updatedAt,
    });
    time = new Date("2026-11-21T10:00:00.000Z");
    const doneA = store.update({
      id: a.id,
      status: "done",
      updatedAt: a.updatedAt,
    });

    const listed = store.list({ statuses: ["done"], orderBy: "doneAt" });

    expect(listed).toEqual([doneA, doneB]);
  });

  it("puts items without a done time last in priority order", () => {
    const [a, b, c] = ["A", "B", "C"].map(addItem);
    const done = store.update({
      id: a.id,
      status: "done",
      updatedAt: a.updatedAt,
    });

    const listed = store.list({ orderBy: "doneAt" });

    expect(listed).toEqual([done, c, b]);
  });

  it("returns items most recently updated first", () => {
    const [a, b] = ["A", "B"].map(addItem);
    time = new Date("2026-11-21T10:00:00.000Z");
    const renamed = store.update({
      id: b.id,
      name: "Renamed",
      updatedAt: b.updatedAt,
    });

    const listed = store.list({ orderBy: "updatedAt" });

    expect(listed).toEqual([renamed, a]);
  });
});

describe("list deleted items", () => {
  it("returns deleted items most recently deleted first", () => {
    const [a, b] = ["A", "B"].map(addItem);
    const deletedB = store.delete(b.id);
    time = new Date("2026-11-21T10:00:00.000Z");
    const deletedA = store.delete(a.id);

    const listed = store.list({ deleted: "only", orderBy: "updatedAt" });

    expect(listed).toEqual([deletedA, deletedB]);
  });

  it("leaves out items that are not deleted", () => {
    const [a] = ["A", "B"].map(addItem);
    const deleted = store.delete(a.id);

    const listed = store.list({ deleted: "only" });

    expect(listed).toEqual([deleted]);
  });

  it("returns only the deleted items with the given statuses", () => {
    const [a, b] = ["A", "B"].map(addItem);
    const done = store.update({
      id: a.id,
      status: "done",
      updatedAt: a.updatedAt,
    })!;
    const deletedDone = store.delete(done.id);
    store.delete(b.id);

    const listed = store.list({ statuses: ["done"], deleted: "only" });

    expect(listed).toEqual([deletedDone]);
  });

  it("leaves out a deleted item done on an earlier day", () => {
    const added = addItem("Shipped");
    const done = store.update({
      id: added.id,
      status: "done",
      updatedAt: added.updatedAt,
    })!;
    store.delete(done.id);
    time = new Date(time.getTime() + 2 * 24 * 60 * 60 * 1000);

    const listed = store.list({ deleted: "only" });

    expect(listed).toEqual([]);
  });

  it("includes a deleted item done on an earlier day when not only today's", () => {
    const added = addItem("Shipped");
    const done = store.update({
      id: added.id,
      status: "done",
      updatedAt: added.updatedAt,
    })!;
    const deleted = store.delete(done.id);
    time = new Date(time.getTime() + 2 * 24 * 60 * 60 * 1000);

    const listed = store.list({ deleted: "only", doneTodayOnly: false });

    expect(listed).toEqual([deleted]);
  });

  it("lists deleted items with the others when deleted items are included", () => {
    const [a, b] = ["A", "B"].map(addItem);
    const deleted = store.delete(a.id);

    const listed = store.list({ deleted: "include" });

    expect(listed).toEqual([b, deleted]);
  });
});

describe("list by status order", () => {
  it("puts current work first, then items set aside, then finished ones, each in priority order", () => {
    const [wontDo, done, deferred, delegated, todo, inProgress] = (
      [
        "wont_do",
        "done",
        "deferred",
        "delegated",
        "todo",
        "in_progress",
      ] as const
    ).map((status) => withStatus(addItem(status), status));

    const listed = store.list({ statuses: everyStatus, orderBy: "status" });

    expect(listed).toEqual([
      inProgress,
      todo,
      delegated,
      deferred,
      done,
      wontDo,
    ]);
  });

  it("puts deleted items after the others", () => {
    const [a, b] = ["A", "B"].map(addItem);
    const deleted = store.delete(b.id);

    const listed = store.list({ deleted: "include", orderBy: "status" });

    expect(listed).toEqual([a, deleted]);
  });
});

describe("search", () => {
  function search(text: string): Item[] {
    return store.list({
      search: text,
      statuses: everyStatus,
      doneTodayOnly: false,
      deleted: "include",
    });
  }

  it("finds an item by part of its name, ignoring case", () => {
    const found = addItem("Renew the Domain name");
    addItem("Book a room");

    const results = search("domain");

    expect(results).toEqual([found]);
  });

  it("finds an item by its description", () => {
    const found = store.add("Plan", "Ask Sam about the budget", []);
    addItem("Budgetless");

    const results = search("about the budget");

    expect(results).toEqual([found]);
  });

  it("finds an item by a source's label, link or reference", () => {
    const byLabel = store.add("A", "", [
      { kind: "link", url: "https://example.com/1", label: "Design doc" },
    ]);
    const byUrl = store.add("B", "", [
      { kind: "link", url: "https://example.com/design", label: "Link" },
    ]);
    const byText = store.add("C", "", [
      { kind: "reference", text: "notes/design.md", label: "Notes" },
    ]);
    addItem("D");

    const results = search("design");

    expect(results).toEqual([byText, byUrl, byLabel]);
  });

  it("does not match a source's kind", () => {
    store.add("A", "", [aReference()]);

    const results = search("reference");

    expect(results).toEqual([]);
  });

  it("does not match a source's id", () => {
    const source = store.add("A", "", [aReference()]).sources[0]!;

    const results = search(source.id);

    expect(results).toEqual([]);
  });

  it("needs every word, each matching anywhere in the item", () => {
    const found = store.add(
      "Renew the domain",
      "Before it lapses in March",
      [],
    );
    addItem("Renew the lease");

    const results = search("  march   renew ");

    expect(results).toEqual([found]);
  });

  it("matches a percent sign literally", () => {
    const percent = addItem("Raise prices by 5%");
    addItem("Raise prices by 50");

    const results = search("5%");

    expect(results).toEqual([percent]);
  });

  it("matches an underscore literally", () => {
    const underscore = addItem("Rename user_id");
    addItem("Rename userXid");

    const results = search("user_id");

    expect(results).toEqual([underscore]);
  });

  it("matches a backslash literally", () => {
    addItem("Raise prices by 5%");

    const results = search("\\");

    expect(results).toEqual([]);
  });

  it("finds items in every status, done on any day and deleted", () => {
    const done = withStatus(addItem("Old report"), "done");
    const deleted = store.delete(addItem("Deleted report").id);
    time = new Date(time.getTime() + 2 * 24 * 60 * 60 * 1000);

    const results = search("report");

    expect(results).toEqual([deleted, done]);
  });

  it("combines with the other filters", () => {
    const todo = addItem("Report A");
    withStatus(addItem("Report B"), "deferred");

    const results = store.list({ search: "report" });

    expect(results).toEqual([todo]);
  });

  it("refuses a blank search", () => {
    const blank = () => store.list({ search: "   " });

    expect(blank).toThrow();
  });

  it("refuses a search longer than 200 characters", () => {
    const tooLong = () => store.list({ search: "a".repeat(201) });

    expect(tooLong).toThrow();
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
    const inProgress = store.update({
      id: done.id,
      status: "in_progress",
      updatedAt: done.updatedAt,
    })!;

    const doneAgain = store.update({
      id: done.id,
      status: "done",
      updatedAt: inProgress.updatedAt,
    });

    expect(store.list()).toEqual([doneAgain]);
  });

  it("keeps an in-progress item after midnight", () => {
    const added = addItem("Ongoing");
    time = lateEvening;
    const inProgress = store.update({
      id: added.id,
      status: "in_progress",
      updatedAt: added.updatedAt,
    });

    time = midnight;

    expect(store.list()).toEqual([inProgress]);
  });
});

describe("update", () => {
  it("changes only the given fields and the updated time", () => {
    const item = addItem("Draft");
    time = new Date("2026-11-21T10:00:00.000Z");

    const updated = store.update({
      id: item.id,
      status: "in_progress",
      updatedAt: item.updatedAt,
    });

    expect(updated).toEqual({
      ...item,
      status: "in_progress",
      updatedAt: "2026-11-21T10:00:00.000Z",
    });
    expect(store.list()).toEqual([updated]);
  });

  it("records when an item is marked done", () => {
    const item = addItem("Draft");
    time = new Date("2026-11-21T10:00:00.000Z");

    const done = store.update({
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    });

    expect(done?.doneAt).toBe("2026-11-21T10:00:00.000Z");
  });

  it("clears when an item was done once it is reopened", () => {
    const item = addItem("Draft");
    const done = store.update({
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    })!;
    time = new Date("2026-11-21T10:00:00.000Z");

    const reopened = store.update({
      id: done.id,
      status: "todo",
      updatedAt: done.updatedAt,
    });

    expect(reopened?.doneAt).toBeNull();
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
    const item = store.add("Draft", "Old notes", []);

    const updated = store.update({
      id: item.id,
      description: "New notes",
      updatedAt: item.updatedAt,
    });

    expect(updated?.description).toBe("New notes");
    expect(openItemStore(dataDir).list()).toEqual([updated]);
  });

  it("adds sources after the item's others, leaving out one it has", () => {
    const first = aSource();
    const second = aReference();
    const third = aSource();
    const item = store.add("Draft", "", [first, second]);

    const updated = store.update({
      id: item.id,
      addSources: [{ ...second, label: "Again" }, third],
      updatedAt: item.updatedAt,
    });

    expect(updated?.sources.slice(0, 2)).toEqual(item.sources);
    expect(withoutIds(updated?.sources)).toEqual([first, second, third]);
    expect(openItemStore(dataDir).list()).toEqual([updated]);
  });

  it("removes sources by their ids", () => {
    const item = store.add("Draft", "", [aSource(), aReference(), aSource()]);
    const [link, reference, kept] = item.sources;

    const updated = store.update({
      id: item.id,
      removeSources: [link!.id, reference!.id],
      updatedAt: item.updatedAt,
    });

    expect(updated?.sources).toEqual([kept]);
  });

  it("refuses to remove a source the item does not have", () => {
    const item = store.add("Draft", "", [aSource()]);

    const remove = () =>
      store.update({
        id: item.id,
        removeSources: [crypto.randomUUID()],
        updatedAt: item.updatedAt,
      });

    expect(remove).toThrow(SourceChangeError);
    expect(openItemStore(dataDir).list()).toEqual([item]);
  });

  it("replaces a source in its place, keeping its id", () => {
    const item = store.add("Draft", "", [aSource(), aReference()]);
    const [first, second] = item.sources;
    const replacement = aReference();

    const updated = store.update({
      id: item.id,
      replaceSource: { id: first!.id, to: replacement },
      updatedAt: item.updatedAt,
    });

    expect(updated?.sources).toEqual([
      { ...replacement, id: first!.id },
      second,
    ]);
  });

  it("refuses to replace a source the item does not have, changing nothing", () => {
    const item = store.add("Draft", "", [aSource()]);

    const replace = () =>
      store.update({
        id: item.id,
        name: "Renamed",
        replaceSource: { id: crypto.randomUUID(), to: aReference() },
        updatedAt: item.updatedAt,
      });

    expect(replace).toThrow(SourceChangeError);
    expect(openItemStore(dataDir).list()).toEqual([item]);
  });

  it("refuses to replace a source with one that repeats another", () => {
    const first = aSource();
    const item = store.add("Draft", "", [first, aReference()]);

    const replace = () =>
      store.update({
        id: item.id,
        replaceSource: {
          id: item.sources[1]!.id,
          to: { ...first, label: "Again" },
        },
        updatedAt: item.updatedAt,
      });

    expect(replace).toThrow(SourceChangeError);
    expect(openItemStore(dataDir).list()).toEqual([item]);
  });

  it("relabels a source in place", () => {
    const first = aSource();
    const item = store.add("Draft", "", [first, aReference()]);

    const updated = store.update({
      id: item.id,
      replaceSource: {
        id: item.sources[0]!.id,
        to: { ...first, label: "New" },
      },
      updatedAt: item.updatedAt,
    });

    expect(updated?.sources[0]).toEqual({
      ...first,
      label: "New",
      id: item.sources[0]!.id,
    });
  });

  it("refuses a source change to an item changed since it was read", () => {
    const item = store.add("Draft", "", [aSource()]);
    time = new Date(time.getTime() + 60_000);
    const changed = store.update({
      id: item.id,
      name: "Changed",
      updatedAt: item.updatedAt,
    });

    const remove = () =>
      store.update({
        id: item.id,
        removeSources: [item.sources[0]!.id],
        updatedAt: item.updatedAt,
      });

    expect(remove).toThrow(ItemChangedError);
    expect(openItemStore(dataDir).list()).toEqual([changed]);
  });

  it("keeps an item's sources when other fields change", () => {
    const item = store.add("Draft", "", [aSource(), aReference()]);

    const updated = store.update({
      id: item.id,
      name: "Final",
      updatedAt: item.updatedAt,
    });

    expect(updated?.sources).toEqual(item.sources);
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
    const [a, , c] = ["A", "B", "C"].map(addItem);

    store.move({ id: a.id, neighbourId: c.id, position: "before" });

    expect(names(store.list())).toEqual(["A", "C", "B"]);
  });

  it("places an item after its neighbour", () => {
    const [, b, c] = ["A", "B", "C"].map(addItem);

    store.move({ id: c.id, neighbourId: b.id, position: "after" });

    expect(names(store.list())).toEqual(["B", "C", "A"]);
  });

  it("places an item between its neighbour and the next item", () => {
    const [a, , c, d] = ["A", "B", "C", "D"].map(addItem);

    store.move({ id: a.id, neighbourId: d.id, position: "after" });
    store.move({ id: d.id, neighbourId: c.id, position: "before" });

    expect(names(store.list())).toEqual(["A", "D", "C", "B"]);
  });

  it("changes only the moved item's rank and updated time", () => {
    const [a, b, c] = ["A", "B", "C"].map(addItem);
    time = new Date("2026-11-21T10:00:00.000Z");

    const moved = store.move({
      id: a.id,
      neighbourId: b.id,
      position: "before",
    });

    expect(moved?.updatedAt).toBe("2026-11-21T10:00:00.000Z");
    expect(store.list()).toEqual([c, moved, b]);
  });

  it("leaves an item moved next to itself unchanged", () => {
    const [a, b] = ["A", "B"].map(addItem);
    time = new Date("2026-11-21T10:00:00.000Z");

    const moved = store.move({
      id: b.id,
      neighbourId: b.id,
      position: "before",
    });

    expect(moved).toEqual(b);
    expect(store.list()).toEqual([b, a]);
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
    const [a, b] = ["A", "B"].map(addItem);
    store.delete(b.id);

    const moved = store.move({
      id: b.id,
      neighbourId: a.id,
      position: "before",
    });

    expect(moved).toBeUndefined();
  });

  it("returns nothing when the neighbour is deleted", () => {
    const [a, b] = ["A", "B"].map(addItem);
    store.delete(a.id);

    const moved = store.move({
      id: b.id,
      neighbourId: a.id,
      position: "before",
    });

    expect(moved).toBeUndefined();
    expect(store.list()).toEqual([b]);
  });
});

describe("delete", () => {
  it("returns the deleted item with its update time", () => {
    const item = addItem("Mistake");
    time = new Date("2026-11-21T10:00:00.000Z");

    const deleted = store.delete(item.id);

    expect(deleted).toEqual({
      ...item,
      deleted: true,
      updatedAt: "2026-11-21T10:00:00.000Z",
    });
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
    const [a, b, c] = ["A", "B", "C"].map(addItem);
    store.delete(b.id);
    time = new Date("2026-11-21T10:00:00.000Z");

    const restored = store.restore(b.id);

    const expected = { ...b, updatedAt: "2026-11-21T10:00:00.000Z" };
    expect(restored).toEqual(expected);
    expect(store.list()).toEqual([c, expected, a]);
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
    const [, b] = ["A", "B"].map(addItem);
    store.delete(b.id);
    addItem("C");

    store.restore(b.id);

    expect(names(store.list())).toEqual(["C", "B", "A"]);
  });

  it("puts an item back beside one moved into its place", () => {
    const [a, b, c] = ["A", "B", "C"].map(addItem);
    store.delete(b.id);
    store.move({ id: a.id, neighbourId: c.id, position: "after" });

    store.restore(b.id);

    expect(names(store.list())).toEqual(["C", "A", "B"]);
  });
});
