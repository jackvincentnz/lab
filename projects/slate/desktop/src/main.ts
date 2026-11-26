import { createRequire } from "node:module";
import { dirname, join } from "node:path";
import { isatty } from "node:tty";
import { app, BrowserWindow, dialog, shell, utilityProcess } from "electron";
import { readConfig } from "../../service/src/config.js";
import { type DevServer, readDevServer, waitForServer } from "./devServer.js";
import { failureDialog, lockHeldFailure } from "./failure.js";
import { guardApp } from "./guards.js";
import { type Log, logOutput, openLog } from "./log.js";
import { openAtLogin } from "./loginItem.js";
import { waitUntilListening } from "./service.js";
import {
  readShowWindow,
  type SecondInstanceData,
  secondInstanceShowsWindow,
} from "./window.js";

const serviceEntry = join(import.meta.dirname, "../../service/src/main.js");
const appDir = join(import.meta.dirname, "../../app");
const webDir = join(appDir, "dist");

let log: Log | undefined;

function createWindow(url: string, show: boolean): void {
  const window = new BrowserWindow({
    width: 960,
    height: 720,
    title: "Slate",
    show,
    webPreferences: { sandbox: true },
  });

  void window.loadURL(url);
}

async function start(): Promise<void> {
  const { dataDir } = readConfig(process.env);
  const devServer = readDevServer(process.env);
  // `js_run_devserver`, which `bazel run` starts Slate through, gives it a pipe for stdin, so stdout
  // is what still reaches the terminal.
  const showWindow = readShowWindow(process.env, {
    packaged: app.isPackaged,
    stdoutIsTerminal: isatty(1),
  });
  // Electron keeps its own files, such as caches and local storage, in its user data folder. Run
  // unpackaged, every Electron app shares that folder, so Slate uses the folder that holds its list.
  app.setPath("userData", dataDir);
  // The lock is held per user data folder, so dev mode still runs beside prod mode.
  const secondInstanceData: SecondInstanceData = { showWindow };
  if (!app.requestSingleInstanceLock(secondInstanceData)) {
    const failure = lockHeldFailure(app.isPackaged, dataDir);
    if (failure) {
      throw failure;
    }
    app.quit();
    return;
  }
  // Electron does not keep a second launch for a listener added later, and one can arrive while the
  // service starts.
  let windowReady!: (url: string) => void;
  const windowUrl = new Promise<string>((resolve) => {
    windowReady = resolve;
  });
  app.on("second-instance", (_event, _argv, _cwd, additionalData) => {
    if (secondInstanceShowsWindow(additionalData)) {
      void windowUrl.then(revealWindow);
    }
  });
  // Opened only by the lock's holder, so a second launch cannot move the running log aside.
  const startLog = openLog(dataDir);
  log = startLog;
  await app.whenReady();
  if (!showWindow) {
    app.dock?.hide();
  }
  const openedAtLogin = openAtLogin(app, (message) =>
    startLog.write("main", message),
  );

  const service = utilityProcess.fork(serviceEntry, [], {
    serviceName: "Slate service",
    env: {
      ...process.env,
      SLATE_DATA_DIR: dataDir,
      SLATE_WEB_DIR: webDir,
    },
    stdio: "pipe",
  });
  logOutput(service.stdout!, startLog, "service", process.stdout);
  logOutput(service.stderr!, startLog, "service", process.stderr);
  const serviceUrl = await waitUntilListening(service);
  const logServiceExit = (code: number) =>
    startLog.write("main", `The Slate service exited with code ${code}`);
  service.on("exit", logServiceExit);
  // Quitting stops the service too, which is not a failure.
  app.on("will-quit", () => service.off("exit", logServiceExit));
  const url = devServer
    ? await startDevServer(devServer, new URL(serviceUrl).port)
    : serviceUrl;
  guardApp(app, new URL(url).origin, (target) => shell.openExternal(target));
  // At login, Slate only needs to run. Its window opens when the user opens Slate.
  if (!openedAtLogin) {
    createWindow(url, showWindow);
  }
  windowReady(url);

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow(url, true);
    }
  });
}

function revealWindow(url: string): void {
  void app.dock?.show();
  const [window] = BrowserWindow.getAllWindows();
  if (!window) {
    createWindow(url, true);
    return;
  }
  if (window.isMinimized()) {
    window.restore();
  }
  window.show();
}

async function startDevServer(
  { port, url }: DevServer,
  servicePort: string,
): Promise<string> {
  const vite = dirname(
    createRequire(import.meta.url).resolve("vite/package.json"),
  );
  const devServer = utilityProcess.fork(
    join(vite, "bin/vite.js"),
    ["--host", "127.0.0.1", "--port", port, "--strictPort"],
    {
      serviceName: "Slate web UI dev server",
      cwd: appDir,
      stdio: "inherit",
      env: { ...process.env, SLATE_PORT: servicePort },
    },
  );
  // Without Vite the window has nothing to show, for example when Vite's port is taken.
  const exitWithDevServer = (code: number) =>
    fail(new Error(`The web UI's dev server exited with code ${code}`));
  devServer.on("exit", exitWithDevServer);
  // Quitting stops the dev server too, which is not a failure.
  app.on("will-quit", () => devServer.off("exit", exitWithDevServer));

  await waitForServer(url);
  return url;
}

function fail(error: Error): void {
  console.error(error);
  log?.write("main", error.stack ?? error.message);
  // Nobody sees the console of an installed app.
  if (app.isPackaged) {
    dialog.showErrorBox(
      "Slate could not start",
      failureDialog(error, log?.path),
    );
  }
  app.exit(1);
}

start().catch(fail);

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
