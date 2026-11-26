import { homedir } from "node:os";
import { join, resolve } from "node:path";
import { z } from "zod";

const optional = <T extends z.ZodType>(schema: T) =>
  z.preprocess((value) => (value === "" ? undefined : value), schema);

const environment = z.object({
  SLATE_PROFILE: optional(z.enum(["dev"]).optional()),
  SLATE_DATA_DIR: optional(z.string().optional()),
  SLATE_PORT: optional(z.coerce.number().int().min(0).max(65535).optional()),
  SLATE_WEB_DIR: optional(z.string().optional()),
  BUILD_WORKING_DIRECTORY: optional(z.string().optional()),
  APPDATA: optional(z.string().optional()),
  XDG_CONFIG_HOME: optional(z.string().optional()),
});

export interface Config {
  dataDir: string;
  port: number;
  webDir?: string;
}

export interface Host {
  platform: NodeJS.Platform;
  home: string;
}

export function readConfig(
  env: NodeJS.ProcessEnv,
  host: Host = { platform: process.platform, home: homedir() },
): Config {
  const {
    SLATE_PROFILE,
    SLATE_DATA_DIR,
    SLATE_PORT,
    SLATE_WEB_DIR,
    BUILD_WORKING_DIRECTORY,
    APPDATA,
    XDG_CONFIG_HOME,
  } = environment.parse(env);
  const dev = SLATE_PROFILE === "dev";
  const appData = appDataDir(host, APPDATA, XDG_CONFIG_HOME);
  return {
    dataDir: SLATE_DATA_DIR
      ? // `bazel run` starts in a temporary folder, so a relative path names the caller's folder.
        resolve(BUILD_WORKING_DIRECTORY ?? "", SLATE_DATA_DIR)
      : join(appData, dev ? "Slate (dev)" : "Slate"),
    port: SLATE_PORT ?? (dev ? 5297 : 5197),
    webDir: SLATE_WEB_DIR,
  };
}

/** Electron's `appData` folder, so that the service and the desktop app share one list. */
function appDataDir(
  { platform, home }: Host,
  appData: string | undefined,
  xdgConfigHome: string | undefined,
): string {
  if (platform === "darwin") {
    return join(home, "Library", "Application Support");
  }
  if (platform === "win32") {
    return appData ?? join(home, "AppData", "Roaming");
  }
  return xdgConfigHome ?? join(home, ".config");
}
