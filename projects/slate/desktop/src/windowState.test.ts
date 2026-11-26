import { EventEmitter } from "node:events";
import { mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import {
  placeWindow,
  type Rectangle,
  readWindowState,
  rememberWindowState,
  type WindowState,
  writeWindowState,
} from "./windowState.js";

const laptop: Rectangle = { x: 0, y: 33, width: 1470, height: 923 };
const monitor: Rectangle = { x: 1470, y: 0, width: 2560, height: 1415 };

describe("placeWindow", () => {
  it("maximizes the first window, centred at the default size for when it is restored", () => {
    const placement = placeWindow(undefined, [laptop], laptop);

    expect(placement).toEqual({
      bounds: { x: 135, y: 95, width: 1200, height: 800 },
      minimumSize: { width: 480, height: 400 },
      maximized: true,
    });
  });

  it("shrinks the default size to fit a smaller screen", () => {
    const small: Rectangle = { x: 0, y: 25, width: 1024, height: 743 };

    const { bounds } = placeWindow(undefined, [small], small);

    expect(bounds).toEqual({ x: 0, y: 25, width: 1024, height: 743 });
  });

  it.each([true, false])(
    "reopens at the saved bounds with maximized set to %s",
    (maximized) => {
      const saved: WindowState = {
        bounds: { x: 1600, y: 100, width: 1000, height: 700 },
        maximized,
      };

      const placement = placeWindow(saved, [laptop, monitor], laptop);

      expect(placement).toEqual({
        bounds: saved.bounds,
        minimumSize: { width: 480, height: 400 },
        maximized,
      });
    },
  );

  it("falls back to the first window's placement when no screen shows the saved bounds", () => {
    const saved: WindowState = {
      bounds: { x: 1600, y: 100, width: 1000, height: 700 },
      maximized: false,
    };

    const placement = placeWindow(saved, [laptop], laptop);

    expect(placement).toEqual({
      bounds: { x: 135, y: 95, width: 1200, height: 800 },
      minimumSize: { width: 480, height: 400 },
      maximized: true,
    });
  });

  it("moves a window that is partly off screen onto the screen that shows most of it", () => {
    const saved: WindowState = {
      bounds: { x: 1200, y: 100, width: 1000, height: 700 },
      maximized: false,
    };

    const { bounds } = placeWindow(saved, [laptop, monitor], laptop);

    expect(bounds).toEqual({ x: 1470, y: 100, width: 1000, height: 700 });
  });

  it("grows a saved window that is smaller than the minimum size, keeping it on its screen", () => {
    const saved: WindowState = {
      bounds: { x: 1300, y: 800, width: 300, height: 100 },
      maximized: false,
    };

    const { bounds } = placeWindow(saved, [laptop], laptop);

    expect(bounds).toEqual({ x: 990, y: 556, width: 480, height: 400 });
  });

  it("shrinks a saved window that is larger than its screen", () => {
    const saved: WindowState = {
      bounds: { x: 0, y: 0, width: 2560, height: 1415 },
      maximized: false,
    };

    const { bounds } = placeWindow(saved, [laptop], laptop);

    expect(bounds).toEqual(laptop);
  });
});

describe("readWindowState and writeWindowState", () => {
  let dir: string;

  beforeEach(() => {
    dir = mkdtempSync(join(tmpdir(), "slate-window-"));
  });

  afterEach(() => {
    rmSync(dir, { recursive: true, force: true });
  });

  it("reads the state it wrote", () => {
    const state: WindowState = {
      bounds: { x: 10, y: 20, width: 1000, height: 700 },
      maximized: true,
    };
    writeWindowState(dir, state, vi.fn());

    const read = readWindowState(dir);

    expect(read).toEqual(state);
  });

  it("reads no state before a window was saved", () => {
    const read = readWindowState(dir);

    expect(read).toBeUndefined();
  });

  it.each(["{", '{"bounds":{"x":0,"y":0,"width":0,"height":0}}'])(
    "reads no state from a file that holds %s",
    (text) => {
      writeFileSync(join(dir, "window.json"), text);

      const read = readWindowState(dir);

      expect(read).toBeUndefined();
    },
  );

  it("logs instead of throwing when it cannot save", () => {
    const log = vi.fn<(message: string) => void>();
    const missing = join(dir, "missing");

    writeWindowState(
      missing,
      { bounds: { x: 0, y: 0, width: 1000, height: 700 }, maximized: false },
      log,
    );

    expect(log).toHaveBeenCalledWith(
      expect.stringMatching(
        `^Slate could not save its window state at ${join(missing, "window.json")}: Error: ENOENT`,
      ),
    );
  });
});

function fakeWindow({ maximized = false } = {}) {
  const normalBounds: Rectangle = { x: 10, y: 20, width: 1000, height: 700 };
  const window = Object.assign(new EventEmitter(), {
    isMaximized: () => maximized,
    getNormalBounds: () => normalBounds,
    maximize: vi.fn(),
  });
  const save = vi.fn<(state: WindowState) => void>();
  return { window, normalBounds, save };
}

describe("rememberWindowState", () => {
  it("maximizes a shown window once", () => {
    const { window, save } = fakeWindow();
    rememberWindowState(window, { show: true, maximized: true }, save);

    window.emit("show");

    expect(window.maximize).toHaveBeenCalledTimes(1);
  });

  it("leaves a hidden window unmaximized while it is hidden", () => {
    const { window, save } = fakeWindow();

    rememberWindowState(window, { show: false, maximized: true }, save);

    expect(window.maximize).not.toHaveBeenCalled();
  });

  it("maximizes a hidden window once it is shown", () => {
    const { window, save } = fakeWindow();
    rememberWindowState(window, { show: false, maximized: true }, save);

    window.emit("show");

    expect(window.maximize).toHaveBeenCalled();
  });

  it("leaves a window unmaximized when its placement is not maximized", () => {
    const { window, save } = fakeWindow();

    rememberWindowState(window, { show: true, maximized: false }, save);

    expect(window.maximize).not.toHaveBeenCalled();
  });

  it.each([true, false])(
    "saves the normal bounds and maximized set to %s on close",
    (maximized) => {
      const { window, normalBounds, save } = fakeWindow({ maximized });
      rememberWindowState(window, { show: true, maximized: false }, save);

      window.emit("close");

      expect(save).toHaveBeenCalledWith({ bounds: normalBounds, maximized });
    },
  );

  it("saves a hidden window that was shown later", () => {
    const { window, normalBounds, save } = fakeWindow();
    rememberWindowState(window, { show: false, maximized: false }, save);
    window.emit("show");

    window.emit("close");

    expect(save).toHaveBeenCalledWith({
      bounds: normalBounds,
      maximized: false,
    });
  });

  it("saves nothing for a window that was never shown", () => {
    const { window, save } = fakeWindow();
    rememberWindowState(window, { show: false, maximized: true }, save);

    window.emit("close");

    expect(save).not.toHaveBeenCalled();
  });
});
