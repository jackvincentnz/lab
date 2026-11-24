import { spawn } from "node:child_process";
import { once } from "node:events";
import { createInterface } from "node:readline";

// Bazel passes no X display to tests, and Electron needs one on Linux.
export default async function globalSetup() {
  if (process.platform !== "linux") {
    return;
  }

  const xvfb = spawn("Xvfb", ["-displayfd", "3", "-nolisten", "tcp"], {
    stdio: ["ignore", "inherit", "inherit", "pipe"],
  });
  const display = await Promise.race([
    once(createInterface({ input: xvfb.stdio[3]! }), "line").then(
      ([line]) => line as string,
    ),
    once(xvfb, "exit").then(() => undefined),
  ]);
  if (display === undefined) {
    throw new Error(
      `Xvfb exited with code ${xvfb.exitCode} before it opened a display`,
    );
  }
  process.env["DISPLAY"] = `:${display}`;

  return () => {
    xvfb.kill();
  };
}
