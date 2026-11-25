import { appendFileSync, mkdirSync, renameSync } from "node:fs";
import { join } from "node:path";
import { createInterface } from "node:readline";

export interface Log {
  path: string;
  write(source: string, message: string): void;
}

export function openLog(dir: string, now = () => new Date()): Log {
  mkdirSync(dir, { recursive: true });
  const path = join(dir, "slate.log");
  // Two runs bound the log's size yet keep the run that failed after the user starts Slate again.
  try {
    renameSync(path, join(dir, "slate.old.log"));
  } catch {
    // The first run has no log yet.
  }
  return {
    path,
    write(source, message) {
      // A full disk must not stop Slate from reporting why it quits.
      try {
        appendFileSync(path, `${now().toISOString()} [${source}] ${message}\n`);
      } catch (error) {
        console.error(`Slate cannot write its log at ${path}:`, error);
      }
    },
  };
}

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
