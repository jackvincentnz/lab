import { describe, expect, it } from "vitest";
import { addItemInput, moveItemInput, updateItemInput } from "./schemas.js";

describe("addItemInput", () => {
  it("trims the name", () => {
    const input = addItemInput.parse({ name: "  Plan  " });

    expect(input).toEqual({ name: "Plan" });
  });

  it("rejects a blank name", () => {
    const result = addItemInput.safeParse({ name: "   " });

    expect(result.success).toBe(false);
  });
});

describe("updateItemInput", () => {
  it("rejects an update that changes nothing", () => {
    const result = updateItemInput.safeParse({ id: crypto.randomUUID() });

    expect(result.success).toBe(false);
  });

  it("rejects an unknown status", () => {
    const result = updateItemInput.safeParse({
      id: crypto.randomUUID(),
      status: "blocked",
    });

    expect(result.success).toBe(false);
  });
});

describe("moveItemInput", () => {
  it("rejects moving an item next to itself", () => {
    const id = crypto.randomUUID();

    const result = moveItemInput.safeParse({
      id,
      neighbourId: id,
      position: "before",
    });

    expect(result.success).toBe(false);
  });
});
