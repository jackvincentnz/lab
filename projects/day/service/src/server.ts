import { createServer, type Server } from "node:http";
import { createHTTPHandler } from "@trpc/server/adapters/standalone";
import { isLoopbackHost } from "./host.js";
import { dayRouter } from "./router.js";
import { type ItemStore } from "./store.js";

export function createDayServer(store: ItemStore): Server {
  const handleTrpc = createHTTPHandler({
    router: dayRouter,
    createContext: () => ({ store }),
  });

  return createServer((request, response) => {
    if (!isLoopbackHost(request.headers.host)) {
      response.writeHead(403).end();
      return;
    }
    handleTrpc(request, response);
  });
}
