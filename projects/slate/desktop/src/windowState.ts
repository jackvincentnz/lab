import { readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { z } from "zod";

export interface Rectangle {
  x: number;
  y: number;
  width: number;
  height: number;
}

export interface WindowState {
  bounds: Rectangle;
  maximized: boolean;
}

interface Size {
  width: number;
  height: number;
}

export interface WindowPlacement {
  bounds: Rectangle;
  minimumSize: Size;
  maximized: boolean;
}

interface SlateWindow {
  isMaximized(): boolean;
  getNormalBounds(): Rectangle;
  maximize(): void;
  once(event: "show", listener: () => void): unknown;
  on(event: "close", listener: () => void): unknown;
}

const minimumSize: Size = { width: 480, height: 400 };

const defaultSize: Size = { width: 1200, height: 800 };

const windowState = z.object({
  bounds: z.object({
    x: z.number().int(),
    y: z.number().int(),
    width: z.number().int().positive(),
    height: z.number().int().positive(),
  }),
  maximized: z.boolean(),
});

function statePath(dir: string): string {
  return join(dir, "window.json");
}

export function readWindowState(dir: string): WindowState | undefined {
  try {
    return windowState.parse(JSON.parse(readFileSync(statePath(dir), "utf8")));
  } catch {
    return undefined;
  }
}

export function writeWindowState(
  dir: string,
  state: WindowState,
  log: (message: string) => void,
): void {
  const path = statePath(dir);
  try {
    writeFileSync(path, JSON.stringify(state));
  } catch (error) {
    log(`Slate could not save its window state at ${path}: ${String(error)}`);
  }
}

export function placeWindow(
  saved: WindowState | undefined,
  workAreas: Rectangle[],
  primaryWorkArea: Rectangle,
): WindowPlacement {
  const workArea = saved && largestOverlap(saved.bounds, workAreas);
  if (!saved || !workArea) {
    return {
      bounds: fit(centre(defaultSize, primaryWorkArea), primaryWorkArea),
      minimumSize,
      maximized: true,
    };
  }
  return {
    bounds: fit(saved.bounds, workArea),
    minimumSize,
    maximized: saved.maximized,
  };
}

export function rememberWindowState(
  window: SlateWindow,
  { show, maximized }: { show: boolean; maximized: boolean },
  save: (state: WindowState) => void,
): void {
  let shown = show;
  if (maximized && show) {
    window.maximize();
  }
  window.once("show", () => {
    // `maximize()` also shows a hidden window, which agents and tests open.
    if (maximized && !shown) {
      window.maximize();
    }
    shown = true;
  });
  // Saving a window nobody saw would replace the maximized first window with the default size.
  window.on("close", () => {
    if (shown) {
      save({
        bounds: window.getNormalBounds(),
        maximized: window.isMaximized(),
      });
    }
  });
}

function largestOverlap(
  bounds: Rectangle,
  workAreas: Rectangle[],
): Rectangle | undefined {
  let largest: Rectangle | undefined;
  let largestArea = 0;
  for (const workArea of workAreas) {
    const width =
      Math.min(bounds.x + bounds.width, workArea.x + workArea.width) -
      Math.max(bounds.x, workArea.x);
    const height =
      Math.min(bounds.y + bounds.height, workArea.y + workArea.height) -
      Math.max(bounds.y, workArea.y);
    if (width > 0 && height > 0 && width * height > largestArea) {
      largest = workArea;
      largestArea = width * height;
    }
  }
  return largest;
}

function centre({ width, height }: Size, workArea: Rectangle): Rectangle {
  return {
    x: workArea.x + Math.round((workArea.width - width) / 2),
    y: workArea.y + Math.round((workArea.height - height) / 2),
    width,
    height,
  };
}

function fit(bounds: Rectangle, workArea: Rectangle): Rectangle {
  const width = Math.min(
    Math.max(bounds.width, minimumSize.width),
    workArea.width,
  );
  const height = Math.min(
    Math.max(bounds.height, minimumSize.height),
    workArea.height,
  );
  return {
    x: clamp(bounds.x, workArea.x, workArea.x + workArea.width - width),
    y: clamp(bounds.y, workArea.y, workArea.y + workArea.height - height),
    width,
    height,
  };
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}
