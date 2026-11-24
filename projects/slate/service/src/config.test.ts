import { describe, expect, it } from "vitest";
import { readConfig } from "./config.js";

const mac = { platform: "darwin", home: "/Users/me" } as const;

describe("readConfig", () => {
  it("defaults the port", () => {
    const config = readConfig({ SLATE_DATA_DIR: "/data" }, mac);

    expect(config).toEqual({ dataDir: "/data", port: 5197 });
  });

  it("defaults an empty port", () => {
    const config = readConfig({ SLATE_DATA_DIR: "/data", SLATE_PORT: "" }, mac);

    expect(config.port).toBe(5197);
  });

  it("reads the port", () => {
    const config = readConfig(
      { SLATE_DATA_DIR: "/data", SLATE_PORT: "6000" },
      mac,
    );

    expect(config.port).toBe(6000);
  });

  it("reads port 0", () => {
    const config = readConfig(
      { SLATE_DATA_DIR: "/data", SLATE_PORT: "0" },
      mac,
    );

    expect(config.port).toBe(0);
  });

  it("rejects a port that is not a port number", () => {
    const reading = () =>
      readConfig({ SLATE_DATA_DIR: "/data", SLATE_PORT: "70000" }, mac);

    expect(reading).toThrow(/SLATE_PORT/);
  });

  it("resolves a relative data folder against the folder bazel run started in", () => {
    const config = readConfig(
      { SLATE_DATA_DIR: "data", BUILD_WORKING_DIRECTORY: "/work" },
      mac,
    );

    expect(config.dataDir).toBe("/work/data");
  });

  it("reads the web UI folder", () => {
    const config = readConfig({ SLATE_WEB_DIR: "/web" }, mac);

    expect(config.webDir).toBe("/web");
  });

  it("keeps data in Application Support on macOS", () => {
    const config = readConfig({}, mac);

    expect(config.dataDir).toBe("/Users/me/Library/Application Support/Slate");
  });

  it("defaults an empty data folder", () => {
    const config = readConfig({ SLATE_DATA_DIR: "" }, mac);

    expect(config.dataDir).toBe("/Users/me/Library/Application Support/Slate");
  });

  it("keeps data in APPDATA on Windows", () => {
    const config = readConfig(
      { APPDATA: "C:\\Users\\me\\AppData\\Roaming" },
      { platform: "win32", home: "C:\\Users\\me" },
    );

    expect(config.dataDir).toMatch(/^C:\\Users\\me\\AppData\\Roaming.Slate$/);
  });

  it("keeps data in XDG_CONFIG_HOME on Linux", () => {
    const config = readConfig(
      { XDG_CONFIG_HOME: "/home/me/config" },
      { platform: "linux", home: "/home/me" },
    );

    expect(config.dataDir).toBe("/home/me/config/Slate");
  });

  it("keeps the dev profile's data apart from the real list", () => {
    const config = readConfig({ SLATE_PROFILE: "dev" }, mac);

    expect(config).toEqual({
      dataDir: "/Users/me/Library/Application Support/Slate (dev)",
      port: 5297,
    });
  });

  it("lets the environment override the dev profile", () => {
    const config = readConfig(
      { SLATE_PROFILE: "dev", SLATE_DATA_DIR: "/data", SLATE_PORT: "6000" },
      mac,
    );

    expect(config).toEqual({ dataDir: "/data", port: 6000 });
  });

  it("falls back to ~/.config on Linux", () => {
    const config = readConfig({}, { platform: "linux", home: "/home/me" });

    expect(config.dataDir).toBe("/home/me/.config/Slate");
  });
});
