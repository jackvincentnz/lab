import { once } from "node:events";
import { mkdtempSync, readFileSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { PassThrough } from "node:stream";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { logDir, logOutput, openLog } from "./log.js";

let dir: string;

beforeEach(() => {
  dir = join(mkdtempSync(join(tmpdir(), "slate-log-")), "logs");
});

afterEach(() => {
  rmSync(join(dir, ".."), { recursive: true, force: true });
});

describe("logDir", () => {
  const home = `/Users/someone-${Math.random()}`;

  it("uses the macOS logs folder named after the data folder", () => {
    const dataDir = join(home, "Library", "Application Support", "Slate (dev)");

    const dir = logDir(dataDir, { platform: "darwin", home });

    expect(dir).toBe(join(home, "Library", "Logs", "Slate (dev)"));
  });

  it("keeps the logs of a data folder outside Application Support inside it", () => {
    const dataDir = join(tmpdir(), "slate-e2e-abc");

    const dir = logDir(dataDir, { platform: "darwin", home });

    expect(dir).toBe(join(dataDir, "logs"));
  });

  it("keeps the logs inside the data folder on other platforms", () => {
    const dataDir = join(home, ".config", "Slate");

    const dir = logDir(dataDir, { platform: "linux", home });

    expect(dir).toBe(join(dataDir, "logs"));
  });
});

describe("openLog", () => {
  const now = () => new Date("2026-11-24T01:02:03.004Z");

  it("appends a timestamped line for each message", () => {
    const log = openLog(dir, { now });

    log.write("main", "first");
    log.write("service", "second");

    expect(readFileSync(log.path, "utf8")).toBe(
      "2026-11-24T01:02:03.004Z [main] first\n" +
        "2026-11-24T01:02:03.004Z [service] second\n",
    );
  });

  it("moves a log over the size limit aside before writing", () => {
    const log = openLog(dir, { maxBytes: 10, now });
    log.write("main", "older than the limit");

    log.write("main", "newer");

    expect(readFileSync(join(dir, "slate.old.log"), "utf8")).toBe(
      "2026-11-24T01:02:03.004Z [main] older than the limit\n",
    );
    expect(readFileSync(log.path, "utf8")).toBe(
      "2026-11-24T01:02:03.004Z [main] newer\n",
    );
  });
});

describe("logOutput", () => {
  it("logs and echoes each line of the output", async () => {
    const log = openLog(dir, { now: () => new Date(0) });
    const output = new PassThrough();
    const echo = new PassThrough({ encoding: "utf8" });
    logOutput(output, log, "service", echo);

    output.end("listening\nfailed");
    await once(output, "end");

    expect(readFileSync(log.path, "utf8")).toBe(
      "1970-01-01T00:00:00.000Z [service] listening\n" +
        "1970-01-01T00:00:00.000Z [service] failed\n",
    );
    expect(echo.read()).toBe("listening\nfailed\n");
  });
});
