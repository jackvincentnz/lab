import { EventEmitter } from "node:events";
import { describe, expect, it, vi } from "vitest";
import { guardApp } from "./guards.js";

const origin = "http://127.0.0.1:5297";

function guardedContents() {
  const contents = Object.assign(new EventEmitter(), {
    setWindowOpenHandler:
      vi.fn<(handler: (details: { url: string }) => unknown) => void>(),
    session: {
      setPermissionRequestHandler:
        vi.fn<
          (
            handler: (
              contents: unknown,
              permission: string,
              grant: (granted: boolean) => void,
            ) => void,
          ) => void
        >(),
      setPermissionCheckHandler: vi.fn<(handler: () => boolean) => void>(),
    },
  });
  const app = new EventEmitter();
  const openExternally = vi
    .fn<(url: string) => Promise<void>>()
    .mockResolvedValue(undefined);
  const log = vi.fn<(message: string) => void>();
  guardApp(app, origin, openExternally, log);
  app.emit("web-contents-created", {}, contents);
  return { contents, openExternally, log };
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

describe("guardApp", () => {
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
      "claude://example/session",
    ])("cancels %s", (url) => {
      const { contents } = guardedContents();

      const cancelled = emitCancellable(contents, event, { url });

      expect(cancelled).toBe(true);
    });
  });

  it.each(["https://example.com/docs", "claude://example/session"])(
    "opens a window for %s outside the app instead",
    (url) => {
      const { contents, openExternally } = guardedContents();
      const [openWindow] = contents.setWindowOpenHandler.mock.calls[0]!;

      const response = openWindow({ url });

      expect(response).toEqual({ action: "deny" });
      expect(openExternally).toHaveBeenCalledWith(url);
    },
  );

  it("logs a link that nothing could open", async () => {
    const { contents, openExternally, log } = guardedContents();
    const [openWindow] = contents.setWindowOpenHandler.mock.calls[0]!;
    openExternally.mockRejectedValue(
      new Error("No application found to open URL"),
    );

    openWindow({ url: "claude://example/session" });

    await vi.waitFor(() =>
      expect(log).toHaveBeenCalledWith(
        "Could not open claude://example/session: No application found to open URL",
      ),
    );
  });

  it.each([
    "http://example.com/",
    "http://127.0.0.1:5297/",
    "file:///Applications/Calculator.app",
    "smb://example.com/share",
    "/Users/example/notes/TODO.md",
    "notes/TODO.md:12",
    "vscode://file/Users/example/notes/TODO.md",
    "claudefoo://example",
  ])("denies a window for %s without opening it", (url) => {
    const { contents, openExternally } = guardedContents();
    const [openWindow] = contents.setWindowOpenHandler.mock.calls[0]!;

    const response = openWindow({ url });

    expect(response).toEqual({ action: "deny" });
    expect(openExternally).not.toHaveBeenCalled();
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
