import { type ChildProcess, spawn } from "node:child_process";

/**
 * Runs a command as Electron's `cli.js` does, forwarding SIGINT, SIGTERM and its exit code, and
 * stops it when this process's parent exits.
 */
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
    // A parent already gone at start usually leaves PID 1, launchd or init, as the parent.
    if (process.ppid !== parent || process.ppid === 1) {
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
