import { describe, expect, it } from "vitest";
import { readConfig } from "./config.js";

describe("readConfig", () => {
  it("defaults the port", () => {
    const config = readConfig({ DAY_DATA_DIR: "/data" });

    expect(config).toEqual({ dataDir: "/data", port: 5197 });
  });

  it("reads the port", () => {
    const config = readConfig({ DAY_DATA_DIR: "/data", DAY_PORT: "6000" });

    expect(config.port).toBe(6000);
  });

  it("requires a data folder", () => {
    expect(() => readConfig({})).toThrow(/DAY_DATA_DIR/);
  });

  it("rejects a port that is not a port number", () => {
    expect(() =>
      readConfig({ DAY_DATA_DIR: "/data", DAY_PORT: "70000" }),
    ).toThrow(/DAY_PORT/);
  });
});
