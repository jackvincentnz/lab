export const DEFAULT_PANEL_WIDTH = 440;
export const MIN_PANEL_WIDTH = 360;
const MAX_WINDOW_SHARE = 0.6;
const STORAGE_KEY = "slate.itemPanelWidth";

export function readSavedPanelWidth(): number | undefined {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    const width = Number(saved);
    return saved !== null && Number.isFinite(width) ? width : undefined;
  } catch {
    return undefined;
  }
}

export function savePanelWidth(width: number) {
  try {
    localStorage.setItem(STORAGE_KEY, String(width));
  } catch {
    // The width then lasts until the page is reloaded.
  }
}

export function maxPanelWidth(windowWidth: number): number {
  return Math.round(windowWidth * MAX_WINDOW_SHARE);
}

export function shownPanelWidth(
  saved: number | undefined,
  windowWidth: number,
): number {
  return Math.min(
    Math.max(saved ?? DEFAULT_PANEL_WIDTH, MIN_PANEL_WIDTH),
    maxPanelWidth(windowWidth),
  );
}
