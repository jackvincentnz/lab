import { describe, expect, it } from "@lab/test-utils";
import { dayName, daysSince } from "./dates";

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

describe("dayName", () => {
  const now = new Date(2026, 10, 27, 15);

  it("names a time earlier today", () => {
    expect(dayName(new Date(2026, 10, 27, 0, 1), now)).toBe("Today");
  });

  it("names a time late yesterday", () => {
    expect(dayName(new Date(2026, 10, 26, 23, 59), now)).toBe("Yesterday");
  });

  it("names an earlier day by its date", () => {
    expect(dayName(new Date(2026, 10, 23, 11), now)).toBe(
      "Monday, November 23",
    );
  });

  it("names a day in an earlier year with its year", () => {
    expect(dayName(new Date(2025, 11, 30, 9), now)).toBe(
      "Tuesday, December 30, 2025",
    );
  });
});
