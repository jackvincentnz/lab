import { describe, expect, it } from "vitest";
import { addItemInput, updateItemInput } from "./item.js";

describe("addItemInput", () => {
  it("trims the name", () => {
    const input = addItemInput.parse({ name: "  Plan  " });

    expect(input).toEqual({ name: "Plan", description: "" });
  });

  it("trims the end of the description", () => {
    const input = addItemInput.parse({
      name: "Plan",
      description: "    npm test\n\n",
    });

    expect(input.description).toBe("    npm test");
  });

  it("treats a description of only whitespace as none", () => {
    const input = addItemInput.parse({ name: "Plan", description: " \n " });

    expect(input.description).toBe("");
  });

  it("trims the source's link and label", () => {
    const input = addItemInput.parse({
      name: "Plan",
      source: {
        kind: "link",
        url: " https://example.com/plan ",
        label: " Planning doc ",
      },
    });

    expect(input.source).toEqual({
      kind: "link",
      url: "https://example.com/plan",
      label: "Planning doc",
    });
  });

  it.each([
    "http://example.com/plan",
    "javascript:alert(1)",
    "file:///etc/hosts",
    "example.com/plan",
  ])("rejects a source link that is not https: %s", (url) => {
    const result = addItemInput.safeParse({
      name: "Plan",
      source: { kind: "link", url, label: "Plan" },
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give a link that starts with https://",
    );
  });

  it("rejects a source with a blank label", () => {
    const result = addItemInput.safeParse({
      name: "Plan",
      source: { kind: "link", url: "https://example.com/plan", label: " " },
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give the link a label that is not blank",
    );
  });

  it("rejects a source of an unknown kind", () => {
    const result = addItemInput.safeParse({
      name: "Plan",
      source: { kind: "note", text: "From the standup" },
    });

    expect(result.success).toBe(false);
  });

  it("rejects a blank name", () => {
    const result = addItemInput.safeParse({ name: "   " });

    expect(result.error?.issues[0]?.message).toBe(
      "Give a name that is not blank",
    );
  });
});

describe("updateItemInput", () => {
  const updatedAt = "2026-11-21T09:00:00.000Z";

  it("rejects an update that changes nothing", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      updatedAt,
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give a name, a description, a source or a status to change",
    );
  });

  it("rejects an update without the updatedAt it was made from", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      name: "Plan",
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give the item's updatedAt from list_items or the last tool that returned the item",
    );
  });

  it("accepts an update that changes only the description", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      description: "",
      updatedAt,
    });

    expect(result.success).toBe(true);
  });

  it("accepts an update that only removes the source", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      source: null,
      updatedAt,
    });

    expect(result.success).toBe(true);
  });

  it("rejects an unknown status", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      status: "blocked",
      updatedAt,
    });

    expect(result.success).toBe(false);
  });
});
