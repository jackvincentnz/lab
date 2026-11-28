import { afterEach, describe, expect, it } from "@lab/test-utils";
import type { ItemStatus } from "./api";
import { itemLink, linkedItemId, viewOf } from "./itemLink";
import { item } from "./test/fakes";

describe("itemLink", () => {
  afterEach(() => {
    window.history.replaceState(null, "", "/");
  });

  it("links to an item that the link opens", () => {
    const linked = item({ name: "Plan the week" });

    window.history.replaceState(null, "", itemLink(linked));

    expect(linkedItemId()).toBe(linked.id);
  });

  it("finds no linked item without a link", () => {
    expect(linkedItemId()).toBeUndefined();
  });

  it.each<[ItemStatus, string]>([
    ["todo", "today"],
    ["in_progress", "today"],
    ["done", "done"],
    ["deferred", "deferred"],
    ["delegated", "delegated"],
    ["wont_do", "wontDo"],
  ])("opens a %s item in the %s view", (status, view) => {
    expect(viewOf(item({ status }))).toBe(view);
  });
});
