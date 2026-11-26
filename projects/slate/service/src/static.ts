import { createReadStream } from "node:fs";
import { stat } from "node:fs/promises";
import { type ServerResponse } from "node:http";
import { extname, join, resolve, sep } from "node:path";
import { pipeline } from "node:stream";

const contentTypes: Record<string, string> = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
};

export async function serveFile(
  folder: string,
  pathname: string,
  response: ServerResponse,
): Promise<void> {
  const root = resolve(folder);
  const file = join(
    root,
    decodePath(pathname === "/" ? "/index.html" : pathname),
  );
  const stats = file.startsWith(root + sep)
    ? await stat(file).catch(() => undefined)
    : undefined;

  if (!stats?.isFile()) {
    response.writeHead(404).end();
    return;
  }

  response.writeHead(200, {
    "content-length": stats.size,
    "content-type": contentTypes[extname(file)] ?? "application/octet-stream",
  });
  pipeline(createReadStream(file), response, () => undefined);
}

function decodePath(pathname: string): string {
  try {
    return decodeURIComponent(pathname);
  } catch {
    return "";
  }
}
