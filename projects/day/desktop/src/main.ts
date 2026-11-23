import { join } from "node:path";
import { app, BrowserWindow, utilityProcess } from "electron";
import { readConfig } from "../../service/src/config.js";
import { waitUntilListening } from "./service.js";

const serviceEntry = join(import.meta.dirname, "../../service/src/main.js");
const webDir = join(import.meta.dirname, "../../app/dist");

function createWindow(url: string): void {
  const window = new BrowserWindow({
    width: 960,
    height: 720,
    title: "Day",
    webPreferences: { sandbox: true },
  });

  void window.loadURL(url);
}

async function start(): Promise<void> {
  const { dataDir } = readConfig(process.env);
  // Electron keeps its own files, such as caches and local storage, in its user data folder. Run
  // unpackaged, every Electron app shares that folder, so Day uses the folder that holds its list.
  app.setPath("userData", dataDir);
  // The lock is held per user data folder, so dev mode still runs beside prod mode.
  if (!app.requestSingleInstanceLock()) {
    app.quit();
    return;
  }
  await app.whenReady();

  const service = utilityProcess.fork(serviceEntry, [], {
    serviceName: "Day service",
    env: {
      ...process.env,
      DAY_DATA_DIR: dataDir,
      DAY_WEB_DIR: webDir,
    },
  });
  const url = await waitUntilListening(service);
  createWindow(url);
  // Started from a terminal, the app would otherwise open behind the terminal.
  app.focus({ steal: true });

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow(url);
    }
  });

  app.on("second-instance", () => {
    const [window] = BrowserWindow.getAllWindows();
    if (!window) {
      createWindow(url);
      return;
    }
    if (window.isMinimized()) {
      window.restore();
    }
    window.focus();
  });
}

start().catch((error: unknown) => {
  console.error(error);
  app.exit(1);
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
