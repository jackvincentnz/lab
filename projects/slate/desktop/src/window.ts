interface Launch {
  packaged: boolean;
  stdoutIsTerminal: boolean;
}

export interface SecondInstanceData {
  showWindow: boolean;
}

interface SlateWindow {
  isMinimized(): boolean;
  restore(): void;
  show(): void;
}

interface WindowApp {
  readonly dock?: { show(): unknown };
  on(
    event: "second-instance",
    listener: (
      event: unknown,
      argv: string[],
      workingDirectory: string,
      additionalData: unknown,
    ) => void,
  ): unknown;
  on(event: "activate", listener: () => void): unknown;
}

interface WindowFactory {
  all(): SlateWindow[];
  create(url: string, show: boolean): void;
}

export interface Windows {
  open(url: string, launch: { show: boolean; atLogin: boolean }): void;
}

// On macOS, showing a window brings Slate in front of other apps, so a window that agents or tests
// open would take the keystrokes of someone working in another app.
export function readShowWindow(
  env: NodeJS.ProcessEnv,
  { packaged, stdoutIsTerminal }: Launch,
): boolean {
  return packaged || stdoutIsTerminal || env["SLATE_SHOW_WINDOW"] === "1";
}

export function listenForWindows(
  app: WindowApp,
  windows: WindowFactory,
): Windows {
  let opened!: (url: string) => void;
  const windowUrl = new Promise<string>((resolve) => {
    opened = resolve;
  });

  const reveal = (url: string) => {
    void app.dock?.show();
    const [window] = windows.all();
    if (!window) {
      windows.create(url, true);
      return;
    }
    if (window.isMinimized()) {
      window.restore();
    }
    window.show();
  };

  app.on("second-instance", (_event, _argv, _cwd, additionalData) => {
    if ((additionalData as SecondInstanceData | null)?.showWindow) {
      void windowUrl.then(reveal);
    }
  });
  app.on("activate", () => {
    void windowUrl.then((url) => {
      if (windows.all().length === 0) {
        windows.create(url, true);
      }
    });
  });

  return {
    open(url, { show, atLogin }) {
      if (!atLogin) {
        windows.create(url, show);
      }
      opened(url);
    },
  };
}
