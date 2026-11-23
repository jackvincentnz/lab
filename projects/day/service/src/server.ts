import { createServer, type Server } from "node:http";
import { createHTTPHandler } from "@trpc/server/adapters/standalone";
import { isLoopbackHost } from "./host.js";
import { dayRouter } from "./router.js";
import { serveFile } from "./static.js";
import { type ItemStore } from "./store.js";

// Mantine renders its theme in inline style elements; scripts stay limited to the page's own files.
const contentSecurityPolicy =
  "default-src 'self'; style-src 'self' 'unsafe-inline'";

const apiPath = "/api/";

export interface DayServerOptions {
  webDir?: string;
}

export function createDayServer(
  store: ItemStore,
  { webDir }: DayServerOptions = {},
): Server {
  const handleTrpc = createHTTPHandler({
    router: dayRouter,
    basePath: apiPath,
    createContext: () => ({ store }),
  });

  return createServer((request, response) => {
    if (!isLoopbackHost(request.headers.host)) {
      response.writeHead(403).end();
      return;
    }
    response.setHeader("content-security-policy", contentSecurityPolicy);

    const pathname = URL.parse(request.url ?? "", "http://localhost")?.pathname;
    if (pathname === undefined) {
      response.writeHead(400).end();
    } else if (pathname.startsWith(apiPath)) {
      handleTrpc(request, response);
    } else if (webDir) {
      void serveFile(webDir, pathname, response);
    } else {
      response.writeHead(404).end();
    }
  });
}
