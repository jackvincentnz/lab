export type LoginItemStatus =
  "not-registered" | "enabled" | "requires-approval" | "not-found";

interface LoginItemSettings {
  wasOpenedAtLogin: boolean;
  status: LoginItemStatus;
}

interface LoginItemApp {
  isPackaged: boolean;
  getLoginItemSettings(): LoginItemSettings;
  setLoginItemSettings(settings: { openAtLogin: boolean }): void;
}

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
  if (status === "not-registered" || status === "not-found") {
    app.setLoginItemSettings({ openAtLogin: true });
    const registered = app.getLoginItemSettings().status;
    if (registered !== "enabled") {
      log(`Slate could not register to open at login: ${registered}`);
    }
  }
  return wasOpenedAtLogin;
}
