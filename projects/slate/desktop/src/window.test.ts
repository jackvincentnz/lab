import { EventEmitter } from "node:events";
import { describe, expect, it, vi } from "vitest";
import {
  listenForWindows,
  readShowWindow,
  type SecondInstanceData,
} from "./window.js";

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

function fakeElectron({ minimized = false } = {}) {
  const app = new EventEmitter();
  const open: { isMinimized(): boolean; restore(): void; show(): void }[] = [];
  const windows = {
    all: () => open,
    create: vi.fn<(url: string, show: boolean) => void>(() => {
      open.push({
        isMinimized: () => minimized,
        restore: vi.fn(),
        show: vi.fn(),
      });
    }),
    present: vi.fn(),
  };
  const url = `http://127.0.0.1:${1024 + Math.floor(Math.random() * 60000)}/`;
  return { app, windows, open, url };
}

function settle(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 0));
}

async function launchAgain(
  app: EventEmitter,
  data: SecondInstanceData | null,
): Promise<void> {
  app.emit("second-instance", {}, [], "/", data);
  await settle();
}

async function activate(app: EventEmitter): Promise<void> {
  app.emit("activate");
  await settle();
}

describe("listenForWindows", () => {
  it.each([true, false])(
    "opens the window with show set to %s as the launch asks",
    (show) => {
      const { app, windows, url } = fakeElectron();

      listenForWindows(app, windows).open(url, { show, atLogin: false });

      expect(windows.create).toHaveBeenCalledWith(url, show);
    },
  );

  it("opens no window at login", () => {
    const { app, windows, url } = fakeElectron();

    listenForWindows(app, windows).open(url, { show: true, atLogin: true });

    expect(windows.create).not.toHaveBeenCalled();
  });

  it("shows a hidden window when a second launch shows its own", async () => {
    const { app, windows, open, url } = fakeElectron();
    listenForWindows(app, windows).open(url, { show: false, atLogin: false });

    await launchAgain(app, { showWindow: true });

    expect(open[0]?.show).toHaveBeenCalled();
    expect(windows.present).toHaveBeenCalled();
  });

  it.each([{ showWindow: false }, null])(
    "leaves the window alone for a second launch that sends %o",
    async (data) => {
      const { app, windows, open, url } = fakeElectron();
      listenForWindows(app, windows).open(url, {
        show: false,
        atLogin: false,
      });

      await launchAgain(app, data);

      expect(open[0]?.show).not.toHaveBeenCalled();
      expect(windows.present).not.toHaveBeenCalled();
    },
  );

  it("shows the window once it opens when a second launch arrives while Slate starts", async () => {
    const { app, windows, open, url } = fakeElectron();
    const slateWindows = listenForWindows(app, windows);
    await launchAgain(app, { showWindow: true });

    slateWindows.open(url, { show: false, atLogin: false });
    await settle();

    expect(open[0]?.show).toHaveBeenCalled();
  });

  it("restores and shows a minimized window when a second launch shows its own", async () => {
    const { app, windows, open, url } = fakeElectron({ minimized: true });
    listenForWindows(app, windows).open(url, { show: true, atLogin: false });

    await launchAgain(app, { showWindow: true });

    expect(open[0]?.restore).toHaveBeenCalled();
    expect(open[0]?.show).toHaveBeenCalled();
  });

  it("opens a shown window for a second launch when none is open", async () => {
    const { app, windows, url } = fakeElectron();
    listenForWindows(app, windows).open(url, { show: true, atLogin: true });

    await launchAgain(app, { showWindow: true });

    expect(windows.create).toHaveBeenCalledWith(url, true);
  });

  it("opens a shown window on activate when none is open", async () => {
    const { app, windows, url } = fakeElectron();
    listenForWindows(app, windows).open(url, { show: true, atLogin: true });

    await activate(app);

    expect(windows.create).toHaveBeenCalledWith(url, true);
  });

  it("opens no window at login for an activate that arrives while Slate starts", async () => {
    const { app, windows, url } = fakeElectron();
    const slateWindows = listenForWindows(app, windows);
    await activate(app);

    slateWindows.open(url, { show: true, atLogin: true });
    await settle();

    expect(windows.create).not.toHaveBeenCalled();
  });

  it("shows the window and presents Slate when revealed", async () => {
    const { app, windows, open, url } = fakeElectron();
    const slateWindows = listenForWindows(app, windows);
    slateWindows.open(url, { show: false, atLogin: false });

    slateWindows.reveal();
    await settle();

    expect(open[0]?.show).toHaveBeenCalled();
    expect(windows.present).toHaveBeenCalled();
  });

  it("restores a minimized window when revealed", async () => {
    const { app, windows, open, url } = fakeElectron({ minimized: true });
    const slateWindows = listenForWindows(app, windows);
    slateWindows.open(url, { show: true, atLogin: false });

    slateWindows.reveal();
    await settle();

    expect(open[0]?.restore).toHaveBeenCalled();
    expect(open[0]?.show).toHaveBeenCalled();
  });

  it("opens a shown window when revealed after the window was closed", async () => {
    const { app, windows, open, url } = fakeElectron();
    const slateWindows = listenForWindows(app, windows);
    slateWindows.open(url, { show: true, atLogin: false });
    open.pop();

    slateWindows.reveal();
    await settle();

    expect(windows.create).toHaveBeenLastCalledWith(url, true);
    expect(windows.create).toHaveBeenCalledTimes(2);
  });

  it("opens the window once Slate has started when revealed while it starts", async () => {
    const { app, windows, url } = fakeElectron();
    const slateWindows = listenForWindows(app, windows);
    slateWindows.reveal();

    slateWindows.open(url, { show: true, atLogin: true });
    await settle();

    expect(windows.create).toHaveBeenCalledWith(url, true);
  });

  it("keeps the open window on activate", async () => {
    const { app, windows, url } = fakeElectron();
    listenForWindows(app, windows).open(url, { show: true, atLogin: false });

    await activate(app);

    expect(windows.create).toHaveBeenCalledTimes(1);
  });
});
