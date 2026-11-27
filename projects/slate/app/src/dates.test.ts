import { describe, expect, it } from "@lab/test-utils";
import { daysSince } from "./dates";

describe("daysSince", () => {
  it("counts whole days", () => {
    const days = daysSince(
      new Date("2026-11-22T09:00:00.000Z"),
      new Date("2026-11-27T10:00:00.000Z"),
    );

    expect(days).toBe(5);
  });

  it("counts a part day as none", () => {
    const days = daysSince(
      new Date("2026-11-22T09:00:00.000Z"),
      new Date("2026-11-23T08:59:59.999Z"),
    );

    expect(days).toBe(0);
  });
});
