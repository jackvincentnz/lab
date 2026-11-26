import { type ChildProcess, spawn } from "node:child_process";

const forwardedSignals = ["SIGINT", "SIGTERM", "SIGHUP"] as const;

/**
 * Runs a command the way Electron's `cli.js` does, and stops it when this process's parent exits.
 */
export function launch(
  command: string,
  args: string[],
  parentCheckMs = 500,
): ChildProcess {
  const child = spawn(command, args, { stdio: "inherit" });
  for (const signal of forwardedSignals) {
    process.on(signal, () => child.kill(signal));
  }

  // `js_run_devserver` neither forwards signals to its tool nor stops it when it exits, and
  // Bazelisk stops `bazel run` with SIGKILL, so the parent can exit without telling this process.
  const parent = process.ppid;
  const parentCheck = setInterval(() => {
    if (process.ppid !== parent) {
      clearInterval(parentCheck);
      // Once only, because Electron quits at once on a second SIGTERM, before the service stops.
      child.kill("SIGTERM");
    }
  }, parentCheckMs);

  child.on("exit", (code) => {
    clearInterval(parentCheck);
    process.exitCode = code ?? 1;
  });
  return child;
}
