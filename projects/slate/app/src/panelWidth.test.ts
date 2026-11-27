import { describe, expect, it, vi } from "@lab/test-utils";
import {
  readSavedPanelWidth,
  savePanelWidth,
  shownPanelWidth,
} from "./panelWidth";

describe("readSavedPanelWidth", () => {
  it("finds no width when none is saved", () => {
    expect(readSavedPanelWidth()).toBeUndefined();
  });

  it("finds the saved width", () => {
    savePanelWidth(520);

    expect(readSavedPanelWidth()).toBe(520);
  });

  it("finds no width when the saved value is not a number", () => {
    localStorage.setItem("slate.itemPanelWidth", "wide");

    expect(readSavedPanelWidth()).toBeUndefined();
  });

  it("finds no width when the saved value is empty", () => {
    localStorage.setItem("slate.itemPanelWidth", "");

    expect(readSavedPanelWidth()).toBeUndefined();
  });

  it("finds no width when storage cannot be read", () => {
    vi.spyOn(Storage.prototype, "getItem").mockImplementation(() => {
      throw new DOMException("Blocked", "SecurityError");
    });

    expect(readSavedPanelWidth()).toBeUndefined();
  });
});

describe("savePanelWidth", () => {
  it("saves nothing when storage cannot be written", () => {
    vi.spyOn(Storage.prototype, "setItem").mockImplementation(() => {
      throw new DOMException("Full", "QuotaExceededError");
    });

    expect(() => savePanelWidth(520)).not.toThrow();
  });
});

describe("shownPanelWidth", () => {
  it("shows 440 pixels when no width is saved", () => {
    expect(shownPanelWidth(undefined, 1600)).toBe(440);
  });

  it("shows a saved width that fits the window", () => {
    expect(shownPanelWidth(700, 1600)).toBe(700);
  });

  it("shows at least 360 pixels", () => {
    expect(shownPanelWidth(200, 1600)).toBe(360);
  });

  it("shows at most 60% of the window", () => {
    expect(shownPanelWidth(900, 1000)).toBe(600);
  });
});
