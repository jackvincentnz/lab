import { describe, expect, it, vi } from "vitest";
import { type LoginItemStatus, openAtLogin } from "./loginItem.js";

function loginItemApp({
  isPackaged = true,
  wasOpenedAtLogin = false,
  status = "not-registered" as LoginItemStatus,
  statusAfterRegistering = "enabled" as LoginItemStatus,
} = {}) {
  let current = status;
  const app = {
    isPackaged,
    getLoginItemSettings: vi.fn(() => ({ wasOpenedAtLogin, status: current })),
    setLoginItemSettings: vi.fn(() => {
      current = statusAfterRegistering;
    }),
  };
  const log = vi.fn<(message: string) => void>();
  return { app, log };
}

describe("openAtLogin", () => {
  it("leaves a checkout's build out of the login items", () => {
    const { app, log } = loginItemApp({ isPackaged: false });

    const openedAtLogin = openAtLogin(app, log);

    expect(openedAtLogin).toBe(false);
    expect(app.getLoginItemSettings).not.toHaveBeenCalled();
    expect(app.setLoginItemSettings).not.toHaveBeenCalled();
  });

  it.each<LoginItemStatus>(["not-registered", "not-found"])(
    "registers the installed app when its login item is %s",
    (status) => {
      const { app, log } = loginItemApp({ status });

      openAtLogin(app, log);

      expect(app.setLoginItemSettings).toHaveBeenCalledWith({
        openAtLogin: true,
      });
      expect(log).not.toHaveBeenCalled();
    },
  );

  it.each<LoginItemStatus>(["enabled", "requires-approval"])(
    "leaves a login item that is %s as it is",
    (status) => {
      const { app, log } = loginItemApp({ status });

      openAtLogin(app, log);

      expect(app.setLoginItemSettings).not.toHaveBeenCalled();
    },
  );

  it.each<LoginItemStatus>([
    "not-registered",
    "requires-approval",
    "not-found",
  ])(
    "logs why registering left the login item %s",
    (statusAfterRegistering) => {
      const { app, log } = loginItemApp({ statusAfterRegistering });

      openAtLogin(app, log);

      expect(log).toHaveBeenCalledWith(
        `Slate could not register to open at login: ${statusAfterRegistering}`,
      );
    },
  );

  it.each([true, false])(
    "says whether macOS opened the app at login when that is %s",
    (wasOpenedAtLogin) => {
      const { app, log } = loginItemApp({ wasOpenedAtLogin });

      const openedAtLogin = openAtLogin(app, log);

      expect(openedAtLogin).toBe(wasOpenedAtLogin);
    },
  );
});
