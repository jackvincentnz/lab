import { describe, expect, it, vi } from "@lab/test-utils";
import { count, formatDate, relative } from "./format";

describe("formatDate", () => {
  it("formats a recorded time with its date", () => {
    expect(formatDate(new Date(2026, 4, 17, 9, 30).toISOString())).toContain(
      "2026",
    );
  });

  it.each([undefined, null, ""])("says %j was not recorded", (value) => {
    expect(formatDate(value)).toBe("Not recorded");
  });
});

describe("relative", () => {
  const now = new Date(2026, 4, 17, 9, 30);
  const daysAgo = (days: number) =>
    new Date(2026, 4, 17 - days, 18, 0).toISOString();

  it.each([undefined, null, ""])("says %j was not recorded", (value) => {
    expect(relative(value)).toBe("not recorded");
  });

  it("names today and yesterday with the time", () => {
    vi.useFakeTimers({ now });

    expect(relative(new Date(2026, 4, 17, 0, 5).toISOString())).toMatch(
      /^today, /,
    );
    expect(relative(daysAgo(1))).toMatch(/^yesterday, /);
  });

  it("counts days within the last week", () => {
    vi.useFakeTimers({ now });

    expect(relative(daysAgo(2))).toBe("2 days ago");
    expect(relative(daysAgo(6))).toBe("6 days ago");
  });

  it("gives the date from a week ago or in the future", () => {
    vi.useFakeTimers({ now });

    expect(relative(daysAgo(7))).toContain("2026");
    expect(relative(daysAgo(7))).not.toContain("ago");
    expect(relative(daysAgo(-1))).toContain("2026");
    expect(relative(daysAgo(-1))).not.toMatch(/today|yesterday|ago/);
  });
});

describe("count", () => {
  it("uses the singular for one and the plural otherwise", () => {
    expect(count(1, "problem")).toBe("1 problem");
    expect(count(0, "problem")).toBe("0 problems");
    expect(count(2, "candidate")).toBe("2 candidates");
  });

  it("accepts an irregular plural", () => {
    expect(count(3, "query", "queries")).toBe("3 queries");
  });
});
