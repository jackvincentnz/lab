import { createRequire } from "node:module";
import { dirname, join } from "node:path";
import { isatty } from "node:tty";
import {
  app,
  BrowserWindow,
  dialog,
  Menu,
  screen,
  shell,
  Tray,
  utilityProcess,
} from "electron";
import { readConfig } from "../../service/src/config.js";
import { type DevServer, readDevServer, waitForServer } from "./devServer.js";
import { failureDialog, lockHeldFailure } from "./failure.js";
import { guardApp } from "./guards.js";
import { type Log, logOutput, openLog } from "./log.js";
import { openAtLogin } from "./loginItem.js";
import { showInMenuBar } from "./menuBar.js";
import { waitUntilListening } from "./service.js";
import {
  listenForWindows,
  readShowWindow,
  type SecondInstanceData,
} from "./window.js";
import {
  placeWindow,
  readWindowState,
  rememberWindowState,
  writeWindowState,
} from "./windowState.js";

const serviceEntry = join(import.meta.dirname, "../../service/src/main.js");
const appDir = join(import.meta.dirname, "../../app");
const webDir = join(appDir, "dist");
const iconsDir = join(import.meta.dirname, "../../icons");

let log: Log | undefined;
// Electron removes a tray icon whose object is garbage collected.
let menuBar: Tray | undefined;

function createWindow(dataDir: string, url: string, show: boolean): void {
  const { bounds, minimumSize, maximized } = placeWindow(
    readWindowState(dataDir),
    screen.getAllDisplays().map(({ workArea }) => workArea),
    screen.getPrimaryDisplay().workArea,
  );
  const window = new BrowserWindow({
    ...bounds,
    minWidth: minimumSize.width,
    minHeight: minimumSize.height,
    title: "Slate",
    show,
    webPreferences: { sandbox: true },
  });
  rememberWindowState(window, { show, maximized }, (state) =>
    writeWindowState(dataDir, state, (message) => log?.write("main", message)),
  );

  void window.loadURL(url);
}

async function start(): Promise<void> {
  const { profile, dataDir } = readConfig(process.env);
  const devServer = readDevServer(process.env);
  // `js_run_devserver`, which `bazel run` starts Slate through, gives it a pipe for stdin, so stdout
  // is what still reaches the terminal.
  const showWindow = readShowWindow(process.env, {
    packaged: app.isPackaged,
    stdoutIsTerminal: isatty(1),
  });
  // Electron keeps its own files, such as caches and local storage, in its user data folder. Run
  // unpackaged, every Electron app shares that folder, so Slate uses the folder that holds its list.
  // The lock is held per user data folder too, so dev mode still runs beside prod mode.
  app.setPath("userData", dataDir);
  if (!claimInstance(dataDir, showWindow)) {
    return;
  }
  // Electron does not keep a second launch for a listener added later, and one can arrive while the
  // service starts.
  const windows = listenForWindows(app, {
    all: () => BrowserWindow.getAllWindows(),
    create: (url, show) => createWindow(dataDir, url, show),
    present: () => present(),
  });
  const present = () => {
    void app.dock?.show();
    menuBar ??= addToMenuBar(profile, () => windows.reveal());
  };
  // Opened only by the lock's holder, so a second launch cannot move the running log aside.
  const startLog = openLog(dataDir);
  log = startLog;
  await app.whenReady();
  // A packaged app takes its icon from its bundle. Run from Bazel, Slate is Electron's own app.
  if (!app.isPackaged) {
    app.dock?.setIcon(
      join(iconsDir, profile === "dev" ? "slate-dev.png" : "slate.png"),
    );
  }
  if (showWindow) {
    present();
  } else {
    app.dock?.hide();
  }
  const atLogin = openAtLogin(app, (message) =>
    startLog.write("main", message),
  );
  const serviceUrl = await startService(dataDir, startLog);
  const url = devServer
    ? await startDevServer(devServer, new URL(serviceUrl).port)
    : serviceUrl;
  guardApp(
    app,
    new URL(url).origin,
    (target) => shell.openExternal(target),
    (message) => startLog.write("main", message),
  );
  windows.open(url, { show: showWindow, atLogin });
}

function addToMenuBar(profile: "dev" | "prod", open: () => void): Tray {
  const tray = new Tray(join(iconsDir, "slateTemplate.png"));
  showInMenuBar(tray, (template) => Menu.buildFromTemplate(template), {
    profile,
    open,
    quit: () => app.quit(),
  });
  return tray;
}

function claimInstance(dataDir: string, showWindow: boolean): boolean {
  const secondInstanceData: SecondInstanceData = { showWindow };
  if (app.requestSingleInstanceLock(secondInstanceData)) {
    return true;
  }
  const failure = lockHeldFailure(app.isPackaged, dataDir);
  if (failure) {
    throw failure;
  }
  app.quit();
  return false;
}

async function startService(dataDir: string, startLog: Log): Promise<string> {
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
  const url = await waitUntilListening(service);
  const logServiceExit = (code: number) =>
    startLog.write("main", `The Slate service exited with code ${code}`);
  service.on("exit", logServiceExit);
  // Quitting stops the service too, which is not a failure.
  app.on("will-quit", () => service.off("exit", logServiceExit));
  return url;
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
