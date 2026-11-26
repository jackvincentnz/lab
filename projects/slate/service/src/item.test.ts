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

  it("rejects a blank name", () => {
    const result = addItemInput.safeParse({ name: "   " });

    expect(result.error?.issues[0]?.message).toBe(
      "Give a name that is not blank",
    );
  });
});

describe("updateItemInput", () => {
  it("rejects an update that changes nothing", () => {
    const result = updateItemInput.safeParse({ id: crypto.randomUUID() });

    expect(result.success).toBe(false);
  });

  it("keeps the updatedAt the change was made from", () => {
    const updatedAt = "2026-11-21T09:00:00.000Z";

    const input = updateItemInput.parse({
      id: crypto.randomUUID(),
      name: "Plan",
      updatedAt,
    });

    expect(input.updatedAt).toBe(updatedAt);
  });

  it("accepts an update that changes only the description", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      description: "",
    });

    expect(result.success).toBe(true);
  });

  it("rejects an unknown status", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      status: "blocked",
    });

    expect(result.success).toBe(false);
  });
});
