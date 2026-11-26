import { type ChildProcess, execFileSync, spawn } from "node:child_process";
import { once } from "node:events";
import { createInterface } from "node:readline";
import { fileURLToPath } from "node:url";
import { afterEach, describe, expect, it } from "vitest";

const launchModule = fileURLToPath(new URL("./launch.js", import.meta.url));
const node = process.execPath;
const started: ChildProcess[] = [];
const childPids: number[] = [];

// Prints its PID, then runs until stopped or until it exits with the given code.
const runningChild = "console.log(process.pid); setInterval(() => {}, 1000);";
const exitingChild = (code: number) =>
  `${runningChild} setTimeout(() => process.exit(${code}), 100);`;
// Prints its PID and each SIGTERM it gets, and keeps running.
const countingChild = `${runningChild} process.on("SIGTERM", () => console.log("SIGTERM"));`;

function launcherScript(child = runningChild): string {
  return [
    `import { launch } from ${JSON.stringify(launchModule)};`,
    `launch(${JSON.stringify(node)}, ["-e", ${JSON.stringify(child)}], 50);`,
  ].join("\n");
}

function start(command: string, args: string[]): ChildProcess {
  const childProcess = spawn(command, args, {
    stdio: ["ignore", "pipe", "inherit"],
  });
  started.push(childProcess);
  return childProcess;
}

function startLauncher(child?: string): ChildProcess {
  return start(node, ["--input-type=module", "-e", launcherScript(child)]);
}

async function firstLine(childProcess: ChildProcess): Promise<number> {
  const [line] = await once(
    createInterface({ input: childProcess.stdout! }),
    "line",
  );
  const pid = Number(line);
  childPids.push(pid);
  return pid;
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
  // A launcher that failed to stop its child would leave it running.
  for (const pid of childPids.splice(0).filter(isRunning)) {
    process.kill(pid, "SIGKILL");
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

  it.each(["SIGINT", "SIGTERM"] as const)(
    "forwards %s to the child",
    async (signal) => {
      const launcher = startLauncher();
      const childPid = await firstLine(launcher);
      const exit = once(launcher, "exit");

      launcher.kill(signal);

      await waitUntilStopped(childPid);
      const [code] = await exit;
      expect(code).toBe(1);
    },
  );

  it("sends no SIGTERM of its own when its parent exits after a forwarded one", async () => {
    // The sleep keeps sh from replacing itself with the launcher.
    const parent = start("/bin/sh", [
      "-c",
      `"$0" --input-type=module -e "$1"; sleep 60`,
      node,
      launcherScript(countingChild),
    ]);
    const lines = createInterface({ input: parent.stdout! })[
      Symbol.asyncIterator
    ]();
    const childPid = Number((await lines.next()).value);
    childPids.push(childPid);
    const launcherPid = Number(
      execFileSync("ps", ["-o", "ppid=", "-p", String(childPid)], {
        encoding: "utf8",
      }),
    );
    process.kill(launcherPid, "SIGTERM");
    await lines.next();

    parent.kill("SIGKILL");
    await new Promise((resolve) => setTimeout(resolve, 500));

    process.kill(childPid, "SIGKILL");
    const later: string[] = [];
    for (let line = await lines.next(); !line.done; line = await lines.next()) {
      later.push(line.value);
    }
    expect(later).toEqual([]);
  });

  it("exits with the child's exit code", async () => {
    const launcher = startLauncher(exitingChild(3));

    const [code] = await once(launcher, "exit");

    expect(code).toBe(3);
  });
});
