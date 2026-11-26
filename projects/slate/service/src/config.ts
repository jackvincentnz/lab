import { readFileSync } from "node:fs";
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
  BUILD_WORKSPACE_DIRECTORY: optional(z.string().optional()),
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
  readGitFile(workspace: string): string | undefined;
}

const localHost: Host = {
  platform: process.platform,
  home: homedir(),
  readGitFile(workspace) {
    try {
      return readFileSync(join(workspace, ".git"), "utf8");
    } catch {
      return undefined;
    }
  },
};

export function readConfig(
  env: NodeJS.ProcessEnv,
  host: Host = localHost,
): Config {
  const {
    SLATE_PROFILE,
    SLATE_DATA_DIR,
    SLATE_PORT,
    SLATE_WEB_DIR,
    BUILD_WORKING_DIRECTORY,
    BUILD_WORKSPACE_DIRECTORY,
    APPDATA,
    XDG_CONFIG_HOME,
  } = environment.parse(env);
  const dev = SLATE_PROFILE === "dev";
  const appData = appDataDir(host, APPDATA, XDG_CONFIG_HOME);
  return {
    dataDir: SLATE_DATA_DIR
      ? // `bazel run` starts in a temporary folder, so a relative path names the caller's folder.
        resolve(BUILD_WORKING_DIRECTORY ?? "", SLATE_DATA_DIR)
      : dev
        ? devDataDir(host, appData, BUILD_WORKSPACE_DIRECTORY)
        : join(appData, "Slate"),
    port: SLATE_PORT ?? (dev ? 5297 : 5197),
    webDir: SLATE_WEB_DIR,
  };
}

/**
 * A build from a linked worktree keeps its data inside the worktree, so that a draft migration
 * never reaches the shared dev folder and removing the worktree removes its data too.
 */
function devDataDir(
  host: Host,
  appData: string,
  workspace: string | undefined,
): string {
  return workspace && isLinkedWorktree(host.readGitFile(workspace))
    ? join(workspace, "projects", "slate", ".dev-data")
    : join(appData, "Slate (dev)");
}

/**
 * A linked worktree's `.git` is a file that points into the main checkout's `.git/worktrees`. The
 * main checkout's `.git` is a folder, and a submodule's file points into `.git/modules`.
 */
export function isLinkedWorktree(gitFile: string | undefined): boolean {
  return /^gitdir: .*\/worktrees\/[^/]+\s*$/.test(gitFile ?? "");
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
