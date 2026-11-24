import { describe, expect, it } from "vitest";
import { isLoopbackHost, isServiceOrigin } from "./host.js";

describe("isLoopbackHost", () => {
  it.each(["localhost:5197", "127.0.0.1:5197", "[::1]:5197", "localhost"])(
    "accepts %s",
    (host) => {
      expect(isLoopbackHost(host)).toBe(true);
    },
  );

  it.each([
    undefined,
    "",
    "attacker.example:5197",
    "localhost.attacker.example",
    "localhost@attacker.example",
    "127.0.0.1.nip.io",
  ])("rejects %s", (host) => {
    expect(isLoopbackHost(host)).toBe(false);
  });
});

describe("isServiceOrigin", () => {
  it.each([undefined, "http://localhost:5197"])("accepts %s", (origin) => {
    expect(isServiceOrigin(origin, "localhost:5197")).toBe(true);
  });

  it.each([
    "https://attacker.example",
    "http://localhost:5196",
    "http://127.0.0.1:5197",
    "null",
  ])("rejects %s", (origin) => {
    expect(isServiceOrigin(origin, "localhost:5197")).toBe(false);
  });
});
