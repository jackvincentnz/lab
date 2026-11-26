import { type ChildProcess, spawn } from "node:child_process";

const forwardedSignals = ["SIGINT", "SIGTERM", "SIGHUP"] as const;

/**
 * Runs a command, forwarding signals and its exit code, and stops it when this process's parent exits.
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
    // A parent already gone at start usually leaves PID 1, launchd or init, as the parent.
    if (process.ppid !== parent || process.ppid === 1) {
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
