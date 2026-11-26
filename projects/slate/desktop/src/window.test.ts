import { describe, expect, it } from "vitest";
import { readShowWindow, secondInstanceShowsWindow } from "./window.js";

describe("readShowWindow", () => {
  it("shows the installed app's window", () => {
    const show = readShowWindow(
      {},
      { packaged: true, stdoutIsTerminal: false },
    );

    expect(show).toBe(true);
  });

  it("shows the window of a checkout's build started from a terminal", () => {
    const show = readShowWindow(
      {},
      { packaged: false, stdoutIsTerminal: true },
    );

    expect(show).toBe(true);
  });

  it("hides the window of a checkout's build started without a terminal", () => {
    const show = readShowWindow(
      {},
      { packaged: false, stdoutIsTerminal: false },
    );

    expect(show).toBe(false);
  });

  it("shows the window when SLATE_SHOW_WINDOW asks for it", () => {
    const show = readShowWindow(
      { SLATE_SHOW_WINDOW: "1" },
      { packaged: false, stdoutIsTerminal: false },
    );

    expect(show).toBe(true);
  });
});

describe("secondInstanceShowsWindow", () => {
  it("shows the window for a second launch that shows its own", () => {
    const show = secondInstanceShowsWindow({ showWindow: true });

    expect(show).toBe(true);
  });

  it("leaves the window alone for a second launch that runs hidden", () => {
    const show = secondInstanceShowsWindow({ showWindow: false });

    expect(show).toBe(false);
  });

  it("leaves the window alone for a second launch that sends no data", () => {
    const show = secondInstanceShowsWindow(null);

    expect(show).toBe(false);
  });
});
