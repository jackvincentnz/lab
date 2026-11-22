import { describe, expect, it } from "vitest";
import { readConfig } from "./config.js";

const mac = { platform: "darwin", home: "/Users/me" } as const;

describe("readConfig", () => {
  it("defaults the port", () => {
    const config = readConfig({ DAY_DATA_DIR: "/data" }, mac);

    expect(config).toEqual({ dataDir: "/data", port: 5197 });
  });

  it("defaults an empty port", () => {
    const config = readConfig({ DAY_DATA_DIR: "/data", DAY_PORT: "" }, mac);

    expect(config.port).toBe(5197);
  });

  it("reads the port", () => {
    const config = readConfig({ DAY_DATA_DIR: "/data", DAY_PORT: "6000" }, mac);

    expect(config.port).toBe(6000);
  });

  it("rejects a port that is not a port number", () => {
    const reading = () =>
      readConfig({ DAY_DATA_DIR: "/data", DAY_PORT: "70000" }, mac);

    expect(reading).toThrow(/DAY_PORT/);
  });

  it("keeps data in Application Support on macOS", () => {
    const config = readConfig({}, mac);

    expect(config.dataDir).toBe("/Users/me/Library/Application Support/Day");
  });

  it("defaults an empty data folder", () => {
    const config = readConfig({ DAY_DATA_DIR: "" }, mac);

    expect(config.dataDir).toBe("/Users/me/Library/Application Support/Day");
  });

  it("keeps data in APPDATA on Windows", () => {
    const config = readConfig(
      { APPDATA: "C:\\Users\\me\\AppData\\Roaming" },
      { platform: "win32", home: "C:\\Users\\me" },
    );

    expect(config.dataDir).toMatch(/^C:\\Users\\me\\AppData\\Roaming.Day$/);
  });

  it("keeps data in XDG_CONFIG_HOME on Linux", () => {
    const config = readConfig(
      { XDG_CONFIG_HOME: "/home/me/config" },
      { platform: "linux", home: "/home/me" },
    );

    expect(config.dataDir).toBe("/home/me/config/Day");
  });

  it("keeps the dev profile's data apart from the real list", () => {
    const config = readConfig({ DAY_PROFILE: "dev" }, mac);

    expect(config).toEqual({
      dataDir: "/Users/me/Library/Application Support/Day Dev",
      port: 5297,
    });
  });

  it("lets the environment override the dev profile", () => {
    const config = readConfig(
      { DAY_PROFILE: "dev", DAY_DATA_DIR: "/data", DAY_PORT: "6000" },
      mac,
    );

    expect(config).toEqual({ dataDir: "/data", port: 6000 });
  });

  it("falls back to ~/.config on Linux", () => {
    const config = readConfig({}, { platform: "linux", home: "/home/me" });

    expect(config.dataDir).toBe("/home/me/.config/Day");
  });
});
