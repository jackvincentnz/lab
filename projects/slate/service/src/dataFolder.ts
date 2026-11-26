import { existsSync, realpathSync, rmSync } from "node:fs";
import { resolve } from "node:path";
import { type Config } from "./config.js";
import { addSampleItems } from "./sampleItems.js";
import { databaseFile, type ItemStore, openItemStore } from "./store.js";

export type DataFolder = Pick<Config, "profile" | "dataDir">;

export function openStore(
  { profile, dataDir }: DataFolder,
  now?: () => Date,
): ItemStore {
  if (profile === "prod") {
    return openItemStore(dataDir, now);
  }

  const fresh = !existsSync(databaseFile(dataDir));
  const store = openItemStore(dataDir, now);
  if (fresh) {
    addSampleItems(store);
  }
  return store;
}

export function resetToSampleItems(dataDir: string, prodDataDir: string): void {
  if (canonicalPath(dataDir) === canonicalPath(prodDataDir)) {
    throw new Error(
      `${dataDir} holds the real data, so it is not replaced with sample items.`,
    );
  }

  const file = databaseFile(dataDir);
  for (const suffix of ["", "-journal", "-wal", "-shm"]) {
    rmSync(`${file}${suffix}`, { force: true });
  }
  openStore({ profile: "dev", dataDir });
}

/** Follows symbolic links and the file system's own letter case, so that no other name reaches the real data. */
function canonicalPath(path: string): string {
  try {
    return realpathSync.native(path);
  } catch {
    return resolve(path);
  }
}
