import {
  existsSync,
  mkdirSync,
  mkdtempSync,
  rmSync,
  symlinkSync,
  writeFileSync,
} from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { openStore, resetToSampleItems } from "./dataFolder.js";
import { openItemStore } from "./store.js";

let root: string;
let dataDir: string;
let prodDataDir: string;

beforeEach(() => {
  root = mkdtempSync(join(tmpdir(), "slate-data-folder-"));
  dataDir = join(root, "Slate (dev)");
  prodDataDir = join(root, "Slate");
});

afterEach(() => {
  rmSync(root, { recursive: true, force: true });
});

const listedSamples = [
  "Write the launch announcement",
  "Review the onboarding checklist",
  "Plan next quarter's goals",
  "Fix the flaky export test",
  "Renew the team's domain name",
  "Answer the survey about desk bookings",
  "Book a room for the design review",
];

function listedNames(folder: string): string[] {
  return openItemStore(folder)
    .list()
    .map(({ name }) => name);
}

describe("openStore for dev", () => {
  it("fills a new folder with the sample items", () => {
    const store = openStore({ profile: "dev", dataDir });

    expect(store.list().map(({ name }) => name)).toEqual(listedSamples);
  });

  it("leaves existing data as it is", () => {
    openItemStore(dataDir).add("Mine", "", []);

    const store = openStore({ profile: "dev", dataDir });

    expect(store.list().map(({ name }) => name)).toEqual(["Mine"]);
  });
});

describe("openStore for prod", () => {
  it("leaves a new folder empty", () => {
    const store = openStore({ profile: "prod", dataDir: prodDataDir });

    expect(store.list()).toEqual([]);
  });
});

describe("resetToSampleItems", () => {
  it("replaces the data with the sample items", () => {
    openItemStore(dataDir).add("Mine", "", []);

    resetToSampleItems(dataDir, prodDataDir);

    expect(listedNames(dataDir)).toEqual(listedSamples);
  });

  it("fills a folder that does not exist yet", () => {
    resetToSampleItems(dataDir, prodDataDir);

    expect(listedNames(dataDir)).toEqual(listedSamples);
  });

  it("refuses the folder of the real data", () => {
    openItemStore(prodDataDir).add("Real", "", []);

    const reset = () =>
      resetToSampleItems(join(prodDataDir, "..", "Slate"), prodDataDir);

    expect(reset).toThrow(/holds the real data/);
    expect(listedNames(prodDataDir)).toEqual(["Real"]);
  });

  it("refuses a link to the folder of the real data", () => {
    openItemStore(prodDataDir).add("Real", "", []);
    const link = join(root, "link");
    symlinkSync(prodDataDir, link);

    const reset = () => resetToSampleItems(link, prodDataDir);

    expect(reset).toThrow(/holds the real data/);
    expect(listedNames(prodDataDir)).toEqual(["Real"]);
  });

  it("keeps the folder's other files", () => {
    const log = join(dataDir, "slate.log");
    mkdirSync(dataDir);
    writeFileSync(log, "");

    resetToSampleItems(dataDir, prodDataDir);

    expect(existsSync(log)).toBe(true);
  });
});
