interface GuardedEvent {
  preventDefault(): void;
}

interface NavigationEvent extends GuardedEvent {
  url: string;
}

interface GuardedContents {
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
    setPermissionCheckHandler(
      handler: (contents: unknown, permission: string) => boolean,
    ): void;
  };
}

interface GuardedApp {
  on(
    event: "web-contents-created",
    listener: (event: unknown, contents: GuardedContents) => void,
  ): unknown;
}

export function guardApp(
  app: GuardedApp,
  origin: string,
  openExternally: (url: string) => Promise<void>,
  log: (message: string) => void,
): void {
  app.on("web-contents-created", (_event, contents) =>
    guardWebContents(contents, origin, openExternally, log),
  );
}

function guardWebContents(
  contents: GuardedContents,
  origin: string,
  openExternally: (url: string) => Promise<void>,
  log: (message: string) => void,
): void {
  const cancelOffOrigin = (event: NavigationEvent) => {
    if (URL.parse(event.url)?.origin !== origin) {
      event.preventDefault();
    }
  };
  contents.on("will-navigate", cancelOffOrigin);
  contents.on("will-redirect", cancelOffOrigin);
  contents.setWindowOpenHandler(({ url }) => {
    if (opensExternally(url)) {
      openExternally(url).catch((error: Error) =>
        log(`Could not open ${url}: ${error.message}`),
      );
    }
    return { action: "deny" };
  });
  contents.on("will-attach-webview", (event) => event.preventDefault());
  // Without handlers, Electron grants every permission, such as the camera or location.
  contents.session.setPermissionRequestHandler((_contents, permission, grant) =>
    grant(isGranted(permission)),
  );
  contents.session.setPermissionCheckHandler((_contents, permission) =>
    isGranted(permission),
  );
}

function isGranted(permission: string): boolean {
  return permission === "clipboard-sanitized-write";
}

function opensExternally(url: string): boolean {
  const protocol = URL.parse(url)?.protocol;
  return protocol === "https:" || protocol === "claude:";
}
