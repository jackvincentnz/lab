import { once } from "node:events";
import { mkdirSync, mkdtempSync, readFileSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { PassThrough } from "node:stream";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { logOutput, openLog } from "./log.js";

let dir: string;

beforeEach(() => {
  dir = mkdtempSync(join(tmpdir(), "slate-log-"));
});

afterEach(() => {
  rmSync(dir, { recursive: true, force: true });
});

describe("openLog", () => {
  const now = () => new Date("2026-11-24T01:02:03.004Z");

  it("appends a timestamped line for each message", () => {
    const log = openLog(dir, now);

    log.write("main", "first");
    log.write("service", "second");

    expect(readFileSync(log.path, "utf8")).toBe(
      "2026-11-24T01:02:03.004Z [main] first\n" +
        "2026-11-24T01:02:03.004Z [service] second\n",
    );
  });

  it("keeps the previous run's log aside", () => {
    openLog(dir, now).write("main", "previous run");

    const log = openLog(dir, now);
    log.write("main", "this run");

    expect(readFileSync(join(dir, "slate.old.log"), "utf8")).toBe(
      "2026-11-24T01:02:03.004Z [main] previous run\n",
    );
    expect(readFileSync(log.path, "utf8")).toBe(
      "2026-11-24T01:02:03.004Z [main] this run\n",
    );
  });

  it("reports a write that fails to the console instead of throwing", () => {
    const logError = vi
      .spyOn(console, "error")
      .mockImplementation(() => undefined);
    const log = openLog(dir, now);
    mkdirSync(log.path);

    log.write("main", "lost");

    expect(logError).toHaveBeenCalledWith(
      `Slate cannot write its log at ${log.path}:`,
      expect.objectContaining({ code: "EISDIR" }),
    );
  });
});

describe("logOutput", () => {
  it("logs and echoes each line of the output", async () => {
    const log = openLog(dir, () => new Date(0));
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
