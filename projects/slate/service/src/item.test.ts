import { describe, expect, it } from "vitest";
import { addItemInput, updateItemInput } from "./item.js";

describe("addItemInput", () => {
  it("trims the name", () => {
    const input = addItemInput.parse({ name: "  Plan  " });

    expect(input).toEqual({ name: "Plan", description: "", sources: [] });
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
      sources: [
        {
          kind: "link",
          url: " https://example.com/plan ",
          label: " Planning doc ",
        },
      ],
    });

    expect(input.sources).toEqual([
      {
        kind: "link",
        url: "https://example.com/plan",
        label: "Planning doc",
      },
    ]);
  });

  it.each([
    "http://example.com/plan",
    "javascript:alert(1)",
    "file:///etc/hosts",
    "example.com/plan",
  ])("rejects a source link that is not https: %s", (url) => {
    const result = addItemInput.safeParse({
      name: "Plan",
      sources: [{ kind: "link", url, label: "Plan" }],
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give a link that starts with https://",
    );
  });

  it("rejects a source with a blank label", () => {
    const result = addItemInput.safeParse({
      name: "Plan",
      sources: [{ kind: "link", url: "https://example.com/plan", label: " " }],
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give the link a label that is not blank",
    );
  });

  it.each(["notes/TODO.md:12", "claude://claude.ai/chat/plan", "Standup"])(
    "keeps a reference source exactly as given, trimmed: %s",
    (text) => {
      const input = addItemInput.parse({
        name: "Plan",
        sources: [{ kind: "reference", text: ` ${text} `, label: " Notes " }],
      });

      expect(input.sources).toEqual([
        { kind: "reference", text, label: "Notes" },
      ]);
    },
  );

  it.each([
    [" ", "Give a reference that is not blank"],
    ["a".repeat(1001), "Give a reference of at most 1000 characters"],
    ["notes/TODO.md\nnotes/plan.md", "Give a reference on one line"],
  ])("rejects the reference %j", (text, message) => {
    const result = addItemInput.safeParse({
      name: "Plan",
      sources: [{ kind: "reference", text, label: "Notes" }],
    });

    expect(result.error?.issues[0]?.message).toBe(message);
  });

  it("accepts a reference of 1000 characters", () => {
    const result = addItemInput.safeParse({
      name: "Plan",
      sources: [{ kind: "reference", text: "a".repeat(1000), label: "Notes" }],
    });

    expect(result.success).toBe(true);
  });

  it("rejects a reference with a blank label", () => {
    const result = addItemInput.safeParse({
      name: "Plan",
      sources: [{ kind: "reference", text: "notes/TODO.md", label: " " }],
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give the reference a label that is not blank",
    );
  });

  it("rejects a source of an unknown kind", () => {
    const result = addItemInput.safeParse({
      name: "Plan",
      sources: [{ kind: "note", text: "From the standup" }],
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
      "Give a name, a description, sources to add, remove or replace, or a status to change",
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

  it("accepts an update that only removes a source", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      removeSources: ["https://example.com/plan"],
      updatedAt,
    });

    expect(result.success).toBe(true);
  });

  it("rejects a replacement that is not a valid source", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      replaceSource: {
        from: "https://example.com/plan",
        to: { kind: "link", url: "http://example.com/plan", label: "Plan" },
      },
      updatedAt,
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give a link that starts with https://",
    );
  });

  it("rejects a blank source to remove", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      removeSources: [" "],
      updatedAt,
    });

    expect(result.error?.issues[0]?.message).toBe(
      "Give a link's url or a reference's text that is not blank",
    );
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
