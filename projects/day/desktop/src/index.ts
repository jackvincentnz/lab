import { join } from "node:path";
import { app, BrowserWindow } from "electron";

const windowPage = join(import.meta.dirname, "../../app/dist/index.html");

function createWindow(): void {
  const window = new BrowserWindow({
    width: 960,
    height: 720,
    title: "Day",
    webPreferences: { sandbox: true },
  });

  void window.loadFile(windowPage);
}

void app.whenReady().then(() => {
  createWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
