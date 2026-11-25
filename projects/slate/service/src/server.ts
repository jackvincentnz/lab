import { createServer, type Server } from "node:http";
import { createHTTPHandler } from "@trpc/server/adapters/standalone";
import { isLoopbackHost, isServiceOrigin } from "./host.js";
import { handleMcpRequest } from "./mcp.js";
import { slateRouter } from "./router.js";
import { serveFile } from "./static.js";
import { type ItemStore } from "./store.js";

// Mantine renders its theme in inline style elements; scripts stay limited to the page's own files.
const contentSecurityPolicy =
  "default-src 'self'; style-src 'self' 'unsafe-inline'";

const apiPath = "/api/";

const mcpPath = "/mcp";

export interface SlateServerOptions {
  webDir?: string;
}

export function createSlateServer(
  store: ItemStore,
  { webDir }: SlateServerOptions = {},
): Server {
  const handleTrpc = createHTTPHandler({
    router: slateRouter,
    basePath: apiPath,
    createContext: () => ({ store }),
    onError: ({ path, error }) =>
      console.error(`The API request to ${path ?? apiPath} failed:`, error),
  });

  return createServer((request, response) => {
    if (!isLoopbackHost(request.headers.host)) {
      response.writeHead(403).end();
      return;
    }
    // A page on another site can send some writes, such as a form POST, to a loopback host without
    // asking first, but its browser names that site in the request's Origin. The MCP specification
    // also requires this check.
    if (
      request.method !== "GET" &&
      !isServiceOrigin(request.headers.origin, request.headers.host)
    ) {
      response.writeHead(403).end();
      return;
    }
    response.setHeader("content-security-policy", contentSecurityPolicy);

    const pathname = URL.parse(request.url ?? "", "http://localhost")?.pathname;
    if (pathname === undefined) {
      response.writeHead(400).end();
    } else if (pathname.startsWith(apiPath)) {
      handleTrpc(request, response);
    } else if (pathname === mcpPath) {
      void handleMcpRequest(store, request, response);
    } else if (webDir) {
      void serveFile(webDir, pathname, response);
    } else {
      response.writeHead(404).end();
    }
  });
}
