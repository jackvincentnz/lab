import { describe, expect, it } from "vitest";
import { failureDialog, lockHeldFailure } from "./failure.js";

const dataDir = `/Users/someone/Library/Application Support/Slate ${Math.random()}`;

describe("lockHeldFailure", () => {
  it("lets the installed app quit quietly while the running one comes forward", () => {
    const failure = lockHeldFailure(true, dataDir);

    expect(failure).toBeUndefined();
  });

  it("names the data folder when a checkout's build finds another Slate running", () => {
    const failure = lockHeldFailure(false, dataDir);

    expect(failure?.message).toContain(dataDir);
  });
});

describe("failureDialog", () => {
  it("gives the reason and the log's path", () => {
    const logPath = `/tmp/slate-${Math.random()}/slate.log`;

    const text = failureDialog(new Error("Port 5197 is in use"), logPath);

    expect(text).toBe(
      `Port 5197 is in use\n\nThe log at ${logPath} has details.`,
    );
  });

  it("gives only the reason before the log is open", () => {
    const text = failureDialog(new Error("SLATE_PORT is invalid"));

    expect(text).toBe("SLATE_PORT is invalid");
  });

  it("gives the reason for a thrown value that is not an error", () => {
    const text = failureDialog("the dev server stopped");

    expect(text).toBe("the dev server stopped");
  });
});
