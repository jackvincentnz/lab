import { EventEmitter } from "node:events";
import { describe, expect, it } from "vitest";
import { waitUntilListening } from "./service.js";

describe("waitUntilListening", () => {
  it("resolves with the URL the service announces", async () => {
    const service = new EventEmitter();
    const url = `http://127.0.0.1:${5000 + Math.floor(Math.random() * 1000)}`;

    const listening = waitUntilListening(service);
    service.emit("message", { url });

    await expect(listening).resolves.toBe(url);
  });

  it("rejects when the service exits first", async () => {
    const service = new EventEmitter();

    const listening = waitUntilListening(service);
    service.emit("exit", 1);

    await expect(listening).rejects.toThrow(/exited with code 1/);
  });
});
