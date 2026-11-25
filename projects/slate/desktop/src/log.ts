import { appendFileSync, mkdirSync, renameSync, statSync } from "node:fs";
import { basename, dirname, join } from "node:path";
import { createInterface } from "node:readline";
import { type Host } from "../../service/src/config.js";

export interface Log {
  path: string;
  write(source: string, message: string): void;
}

export interface LogOptions {
  maxBytes?: number;
  now?: () => Date;
}

/**
 * Where Electron keeps an app's logs: `~/Library/Logs/<name>` on macOS and the user data folder
 * elsewhere. Run unpackaged, Electron names the app Electron, so the folder takes the data folder's
 * name instead. A data folder outside Application Support, such as a test's, keeps its own logs.
 */
export function logDir(dataDir: string, { platform, home }: Host): string {
  if (
    platform === "darwin" &&
    dirname(dataDir) === join(home, "Library", "Application Support")
  ) {
    return join(home, "Library", "Logs", basename(dataDir));
  }
  return join(dataDir, "logs");
}

/** Appends to `slate.log` in `dir`, moving it to `slate.old.log` once it passes `maxBytes`. */
export function openLog(
  dir: string,
  { maxBytes = 1_000_000, now = () => new Date() }: LogOptions = {},
): Log {
  mkdirSync(dir, { recursive: true });
  const path = join(dir, "slate.log");
  return {
    path,
    write(source, message) {
      if ((statSync(path, { throwIfNoEntry: false })?.size ?? 0) > maxBytes) {
        renameSync(path, join(dir, "slate.old.log"));
      }
      appendFileSync(path, `${now().toISOString()} [${source}] ${message}\n`);
    },
  };
}

/** Writes each line of `output` to `log` and to `echo`. */
export function logOutput(
  output: NodeJS.ReadableStream,
  log: Log,
  source: string,
  echo: NodeJS.WritableStream,
): void {
  createInterface({ input: output, crlfDelay: Infinity }).on("line", (line) => {
    log.write(source, line);
    echo.write(`${line}\n`);
  });
}
