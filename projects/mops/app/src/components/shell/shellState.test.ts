import { describe, expect, test } from "../../test";
import {
  ASIDE_MAX_WIDTH,
  ASIDE_MIN_WIDTH,
  clampAsideWidth,
} from "./shellState";

describe("shell state", () => {
  test("clamps the aside width to the minimum", () => {
    expect(clampAsideWidth(ASIDE_MIN_WIDTH - 50)).toBe(ASIDE_MIN_WIDTH);
  });

  test("clamps the aside width to the maximum", () => {
    expect(clampAsideWidth(ASIDE_MAX_WIDTH + 50)).toBe(ASIDE_MAX_WIDTH);
  });

  test("keeps widths that are already in range", () => {
    expect(clampAsideWidth(640)).toBe(640);
  });
});
