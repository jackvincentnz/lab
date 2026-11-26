/** The login item state that Electron's `app.getLoginItemSettings` reports on macOS. */
interface LoginItemSettings {
  wasOpenedAtLogin: boolean;
  status: "not-registered" | "enabled" | "requires-approval" | "not-found";
}

/** The parts of Electron's `app` that manage its login item. */
interface LoginItemApp {
  isPackaged: boolean;
  getLoginItemSettings(): LoginItemSettings;
  setLoginItemSettings(settings: { openAtLogin: boolean }): void;
}

/**
 * Registers the installed app to open at login, so that the list and its MCP endpoint are running
 * before anyone opens Slate. Returns whether this launch was the one macOS made at login.
 */
export function openAtLogin(
  app: LoginItemApp,
  log: (message: string) => void,
): boolean {
  // A checkout's build would register the Electron binary of its worktree, and tests would change
  // the login items of whoever runs them.
  if (!app.isPackaged) {
    return false;
  }
  const { wasOpenedAtLogin, status } = app.getLoginItemSettings();
  // Registering an item that is enabled or waiting for the user's approval would override what the
  // user chose in System Settings.
  if (status === "not-registered" || status === "not-found") {
    app.setLoginItemSettings({ openAtLogin: true });
    const registered = app.getLoginItemSettings().status;
    if (registered !== "enabled") {
      log(`Slate could not register to open at login: ${registered}`);
    }
  }
  return wasOpenedAtLogin;
}
