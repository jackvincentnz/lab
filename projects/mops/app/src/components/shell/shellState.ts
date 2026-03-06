export const ASIDE_MIN_WIDTH = 400;
export const ASIDE_MAX_WIDTH = 1000;
export const ASIDE_OPENED_KEY = "aside-opened";

export function clampAsideWidth(width: number) {
  return Math.max(ASIDE_MIN_WIDTH, Math.min(ASIDE_MAX_WIDTH, width));
}
