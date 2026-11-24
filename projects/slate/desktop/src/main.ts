import { createRequire } from "node:module";
import { dirname, join } from "node:path";
import { app, BrowserWindow, utilityProcess } from "electron";
import { readConfig } from "../../service/src/config.js";
import { type DevServer, readDevServer, waitForServer } from "./devServer.js";
import { waitUntilListening } from "./service.js";

const serviceEntry = join(import.meta.dirname, "../../service/src/main.js");
const appDir = join(import.meta.dirname, "../../app");
const webDir = join(appDir, "dist");

function createWindow(url: string): void {
  const window = new BrowserWindow({
    width: 960,
    height: 720,
    title: "Slate",
    webPreferences: { sandbox: true },
  });

  void window.loadURL(url);
}

async function start(): Promise<void> {
  const { dataDir } = readConfig(process.env);
  const devServer = readDevServer(process.env);
  // Electron keeps its own files, such as caches and local storage, in its user data folder. Run
  // unpackaged, every Electron app shares that folder, so Slate uses the folder that holds its list.
  app.setPath("userData", dataDir);
  // The lock is held per user data folder, so dev mode still runs beside prod mode.
  if (!app.requestSingleInstanceLock()) {
    app.quit();
    return;
  }
  await app.whenReady();

  const service = utilityProcess.fork(serviceEntry, [], {
    serviceName: "Slate service",
    env: {
      ...process.env,
      SLATE_DATA_DIR: dataDir,
      SLATE_WEB_DIR: webDir,
    },
  });
  const serviceUrl = await waitUntilListening(service);
  const url = devServer
    ? await startDevServer(devServer, new URL(serviceUrl).port)
    : serviceUrl;
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
  const exitWithDevServer = (code: number) => {
    console.error(`The web UI's dev server exited with code ${code}`);
    app.exit(1);
  };
  devServer.on("exit", exitWithDevServer);
  // Quitting stops the dev server too, which is not a failure.
  app.on("will-quit", () => devServer.off("exit", exitWithDevServer));

  await waitForServer(url);
  return url;
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
