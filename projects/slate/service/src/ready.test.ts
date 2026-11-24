import { afterEach, describe, expect, it, vi } from "vitest";
import { announceReady } from "./ready.js";

const utilityProcess = process as NodeJS.Process & { parentPort?: unknown };

afterEach(() => {
  delete utilityProcess.parentPort;
});

describe("announceReady", () => {
  it("posts the URL to Electron main", () => {
    const postMessage = vi.fn();
    utilityProcess.parentPort = { postMessage };
    const url = `http://127.0.0.1:${5000 + Math.floor(Math.random() * 1000)}`;

    announceReady(url);

    expect(postMessage).toHaveBeenCalledWith({ url });
  });

  it("does nothing when the service runs on its own", () => {
    const announcing = () => announceReady("http://127.0.0.1:5297");

    expect(announcing).not.toThrow();
  });
});
