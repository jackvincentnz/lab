import { type ChildProcess, spawn } from "node:child_process";

export function launch(
  command: string,
  args: string[],
  parentCheckMs = 500,
): ChildProcess {
  const child = spawn(command, args, { stdio: "inherit" });

  // `js_run_devserver` neither forwards signals to its tool nor stops it when it exits, and
  // Bazelisk stops `bazel run` with SIGKILL, so the parent can exit without telling this process.
  const parent = process.ppid;
  const parentCheck = setInterval(() => {
    if (process.ppid !== parent) {
      stop("SIGTERM");
    }
  }, parentCheckMs);
  // Electron exits at once on a second signal, before the service stops, so a parent that exits
  // after a forwarded signal adds none.
  const stop = (signal: NodeJS.Signals) => {
    clearInterval(parentCheck);
    child.kill(signal);
  };
  process.on("SIGINT", stop);
  process.on("SIGTERM", stop);

  child.on("exit", (code) => {
    clearInterval(parentCheck);
    process.exitCode = code ?? 1;
  });
  return child;
}
