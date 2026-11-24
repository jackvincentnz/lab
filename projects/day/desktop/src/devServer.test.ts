import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { readDevServer, waitForServer } from "./devServer.js";

describe("readDevServer", () => {
  it("returns nothing unless the dev target asks for the dev server", () => {
    const devServer = readDevServer({ DAY_APP_PORT: "6196" });

    expect(devServer).toBeUndefined();
  });

  it("uses the web UI dev server's default port on loopback", () => {
    const devServer = readDevServer({ DAY_WEB_DEV_SERVER: "1" });

    expect(devServer).toEqual({ port: "5196", url: "http://127.0.0.1:5196" });
  });

  it("uses the port in DAY_APP_PORT", () => {
    const devServer = readDevServer({
      DAY_WEB_DEV_SERVER: "1",
      DAY_APP_PORT: "6196",
    });

    expect(devServer).toEqual({ port: "6196", url: "http://127.0.0.1:6196" });
  });

  it.each(["80@example.com", "0", "65536"])(
    "refuses DAY_APP_PORT=%s, so the URL stays on loopback",
    (port) => {
      const env = { DAY_WEB_DEV_SERVER: "1", DAY_APP_PORT: port };

      const read = () => readDevServer(env);

      expect(read).toThrow("DAY_APP_PORT must be a port number");
    },
  );
});

describe("waitForServer", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("resolves once the server answers", async () => {
    const fetch = vi
      .fn<typeof globalThis.fetch>()
      .mockRejectedValueOnce(new TypeError("fetch failed"))
      .mockResolvedValueOnce(new Response());
    vi.stubGlobal("fetch", fetch);

    const waiting = waitForServer("http://127.0.0.1:5196");
    await vi.advanceTimersByTimeAsync(100);
    await waiting;

    expect(fetch).toHaveBeenCalledTimes(2);
  });

  it("rejects when the server never answers", async () => {
    vi.stubGlobal(
      "fetch",
      vi
        .fn<typeof globalThis.fetch>()
        .mockRejectedValue(new TypeError("fetch failed")),
    );

    const waiting = waitForServer("http://127.0.0.1:5196");
    const failure = expect(waiting).rejects.toThrow(
      "The web UI's dev server did not answer at http://127.0.0.1:5196 within 30000 ms",
    );
    await vi.advanceTimersByTimeAsync(30_000);

    await failure;
  });
});
