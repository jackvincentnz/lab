import { type ChildProcess, spawn } from "node:child_process";
import { once } from "node:events";
import { createInterface } from "node:readline";
import { fileURLToPath } from "node:url";
import { afterEach, describe, expect, it } from "vitest";

const launchModule = fileURLToPath(new URL("./launch.js", import.meta.url));
const node = process.execPath;
const started: ChildProcess[] = [];

// Prints its PID, then runs until stopped or until it exits with the given code.
function childScript(exitCode?: number): string {
  const exit =
    exitCode === undefined
      ? ""
      : `setTimeout(() => process.exit(${exitCode}), 100);`;
  return `console.log(process.pid); setInterval(() => {}, 1000); ${exit}`;
}

function launcherScript(exitCode?: number): string {
  return [
    `import { launch } from ${JSON.stringify(launchModule)};`,
    `launch(${JSON.stringify(node)}, ["-e", ${JSON.stringify(childScript(exitCode))}], 50);`,
  ].join("\n");
}

function start(command: string, args: string[]): ChildProcess {
  const childProcess = spawn(command, args, {
    stdio: ["ignore", "pipe", "inherit"],
  });
  started.push(childProcess);
  return childProcess;
}

function startLauncher(exitCode?: number): ChildProcess {
  return start(node, ["--input-type=module", "-e", launcherScript(exitCode)]);
}

async function firstLine(childProcess: ChildProcess): Promise<number> {
  const [line] = await once(
    createInterface({ input: childProcess.stdout! }),
    "line",
  );
  return Number(line);
}

function isRunning(pid: number): boolean {
  try {
    process.kill(pid, 0);
    return true;
  } catch {
    return false;
  }
}

async function waitUntilStopped(pid: number): Promise<void> {
  const deadline = Date.now() + 5000;
  while (isRunning(pid)) {
    if (Date.now() > deadline) {
      throw new Error(`Process ${pid} is still running`);
    }
    await new Promise((resolve) => setTimeout(resolve, 50));
  }
}

afterEach(() => {
  for (const childProcess of started.splice(0)) {
    childProcess.kill("SIGKILL");
  }
});

describe("launch", () => {
  it("stops the child when the launcher's parent is killed", async () => {
    // The sleep keeps sh from replacing itself with the launcher.
    const parent = start("/bin/sh", [
      "-c",
      `"$0" --input-type=module -e "$1"; sleep 60`,
      node,
      launcherScript(),
    ]);
    const childPid = await firstLine(parent);

    parent.kill("SIGKILL");

    await waitUntilStopped(childPid);
  });

  it("forwards SIGTERM to the child", async () => {
    const launcher = startLauncher();
    const childPid = await firstLine(launcher);
    const exit = once(launcher, "exit");

    launcher.kill("SIGTERM");

    await waitUntilStopped(childPid);
    const [code] = await exit;
    expect(code).toBe(1);
  });

  it("exits with the child's exit code", async () => {
    const launcher = startLauncher(3);

    const [code] = await once(launcher, "exit");

    expect(code).toBe(3);
  });
});
