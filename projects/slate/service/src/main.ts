import { readConfig } from "./config.js";
import { listen } from "./listen.js";
import { announceFailure, announceReady } from "./ready.js";
import { createSlateServer } from "./server.js";
import { openItemStore } from "./store.js";

try {
  const { dataDir, port, webDir } = readConfig(process.env);
  const server = createSlateServer(openItemStore(dataDir), { webDir });
  const url = await listen(server, port, "127.0.0.1");
  console.log(`Slate service listening on ${url}`);
  announceReady(url);
} catch (error) {
  console.error("The Slate service cannot start:", error);
  announceFailure((error as Error).message);
  process.exitCode = 1;
}
