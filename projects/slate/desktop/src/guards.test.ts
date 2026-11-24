import { EventEmitter } from "node:events";
import { describe, expect, it, vi } from "vitest";
import { type GuardedContents, guardApp, guardWebContents } from "./guards.js";

const origin = "http://127.0.0.1:5297";

function fakeContents() {
  return Object.assign(new EventEmitter(), {
    setWindowOpenHandler: vi.fn<GuardedContents["setWindowOpenHandler"]>(),
    session: {
      setPermissionRequestHandler:
        vi.fn<GuardedContents["session"]["setPermissionRequestHandler"]>(),
      setPermissionCheckHandler:
        vi.fn<GuardedContents["session"]["setPermissionCheckHandler"]>(),
    },
  });
}

function guardedContents() {
  const contents = fakeContents();
  const openInBrowser = vi.fn<(url: string) => Promise<void>>();
  guardWebContents(contents, origin, openInBrowser);
  return { contents, openInBrowser };
}

function emitCancellable(
  contents: EventEmitter,
  event: string,
  details: object = {},
): boolean {
  const preventDefault = vi.fn();
  contents.emit(event, { ...details, preventDefault });
  return preventDefault.mock.calls.length > 0;
}

describe("guardWebContents", () => {
  describe.each(["will-navigate", "will-redirect"])("on %s", (event) => {
    it.each([
      "http://127.0.0.1:5297/",
      "http://127.0.0.1:5297/items?view=all#top",
    ])("allows %s on Slate's origin", (url) => {
      const { contents } = guardedContents();

      const cancelled = emitCancellable(contents, event, { url });

      expect(cancelled).toBe(false);
    });

    it.each([
      "https://example.com/",
      "http://127.0.0.1:5298/",
      "https://127.0.0.1:5297/",
      "http://localhost:5297/",
      "http://127.0.0.1:5297@example.com/",
      "file:///etc/passwd",
      "about:blank",
    ])("cancels %s", (url) => {
      const { contents } = guardedContents();

      const cancelled = emitCancellable(contents, event, { url });

      expect(cancelled).toBe(true);
    });
  });

  it("opens an https window in the user's browser instead", () => {
    const { contents, openInBrowser } = guardedContents();
    const [openWindow] = contents.setWindowOpenHandler.mock.calls[0]!;

    const response = openWindow({ url: "https://example.com/docs" });

    expect(response).toEqual({ action: "deny" });
    expect(openInBrowser).toHaveBeenCalledWith("https://example.com/docs");
  });

  it.each([
    "http://example.com/",
    "http://127.0.0.1:5297/",
    "file:///Applications/Calculator.app",
    "smb://example.com/share",
  ])("denies a window for %s without opening it", (url) => {
    const { contents, openInBrowser } = guardedContents();
    const [openWindow] = contents.setWindowOpenHandler.mock.calls[0]!;

    const response = openWindow({ url });

    expect(response).toEqual({ action: "deny" });
    expect(openInBrowser).not.toHaveBeenCalled();
  });

  it("cancels every webview", () => {
    const { contents } = guardedContents();

    const cancelled = emitCancellable(contents, "will-attach-webview");

    expect(cancelled).toBe(true);
  });

  it("denies every permission request", () => {
    const { contents } = guardedContents();
    const [handleRequest] =
      contents.session.setPermissionRequestHandler.mock.calls[0]!;
    const grant = vi.fn();

    handleRequest(contents, "media", grant);

    expect(grant).toHaveBeenCalledWith(false);
  });

  it("denies every permission check", () => {
    const { contents } = guardedContents();
    const [handleCheck] =
      contents.session.setPermissionCheckHandler.mock.calls[0]!;

    const granted = handleCheck();

    expect(granted).toBe(false);
  });
});

describe("guardApp", () => {
  it("guards web contents the app creates later", () => {
    const app = new EventEmitter();
    const contents = fakeContents();
    guardApp(app, origin, vi.fn<(url: string) => Promise<void>>());
    app.emit("web-contents-created", {}, contents);

    const cancelled = emitCancellable(contents, "will-navigate", {
      url: "https://example.com/",
    });

    expect(cancelled).toBe(true);
  });
});
