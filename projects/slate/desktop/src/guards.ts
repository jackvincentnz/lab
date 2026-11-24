/** An Electron event that the guards can cancel. */
export interface GuardedEvent {
  preventDefault(): void;
}

/** An Electron navigation event that the guards can cancel. */
export interface NavigationEvent extends GuardedEvent {
  url: string;
}

/** The events and handlers of Electron's `WebContents` that the guards use. */
export interface GuardedContents {
  on(
    event: "will-navigate",
    listener: (event: NavigationEvent) => void,
  ): unknown;
  on(
    event: "will-redirect",
    listener: (event: NavigationEvent) => void,
  ): unknown;
  on(
    event: "will-attach-webview",
    listener: (event: GuardedEvent) => void,
  ): unknown;
  setWindowOpenHandler(
    handler: (details: { url: string }) => { action: "deny" },
  ): void;
  session: {
    setPermissionRequestHandler(
      handler: (
        contents: unknown,
        permission: string,
        grant: (granted: boolean) => void,
      ) => void,
    ): void;
    setPermissionCheckHandler(handler: () => boolean): void;
  };
}

/** The event of Electron's `app` that announces each new web contents. */
export interface GuardedApp {
  on(
    event: "web-contents-created",
    listener: (event: unknown, contents: GuardedContents) => void,
  ): unknown;
}

/** Guards every web contents that `app` creates from now on. */
export function guardApp(
  app: GuardedApp,
  origin: string,
  openInBrowser: (url: string) => Promise<void>,
): void {
  app.on("web-contents-created", (_event, contents) =>
    guardWebContents(contents, origin, openInBrowser),
  );
}

/**
 * Keeps `contents` on the origin that served Slate's page, sends new windows to the user's browser,
 * and denies every web permission.
 */
export function guardWebContents(
  contents: GuardedContents,
  origin: string,
  openInBrowser: (url: string) => Promise<void>,
): void {
  const cancelOffOrigin = (event: NavigationEvent) => {
    if (URL.parse(event.url)?.origin !== origin) {
      event.preventDefault();
    }
  };
  contents.on("will-navigate", cancelOffOrigin);
  contents.on("will-redirect", cancelOffOrigin);
  contents.setWindowOpenHandler(({ url }) => {
    if (URL.parse(url)?.protocol === "https:") {
      void openInBrowser(url);
    }
    return { action: "deny" };
  });
  contents.on("will-attach-webview", (event) => event.preventDefault());
  // Without handlers, Electron grants every permission, such as the camera or location.
  contents.session.setPermissionRequestHandler(
    (_contents, _permission, grant) => grant(false),
  );
  contents.session.setPermissionCheckHandler(() => false);
}
