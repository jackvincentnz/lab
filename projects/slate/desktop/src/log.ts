import { appendFileSync, existsSync, mkdirSync, renameSync } from "node:fs";
import { join } from "node:path";
import { createInterface } from "node:readline";

export interface Log {
  path: string;
  write(source: string, message: string): void;
}

export function openLog(dir: string, now = () => new Date()): Log {
  mkdirSync(dir, { recursive: true });
  const path = join(dir, "slate.log");
  // Starting each run with a new log keeps its size small, and the previous run's log keeps the run
  // that failed after the user starts Slate again.
  if (existsSync(path)) {
    renameSync(path, join(dir, "slate.old.log"));
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
