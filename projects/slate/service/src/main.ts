import { readConfig } from "./config.js";
import { openStore } from "./dataFolder.js";
import { listen } from "./listen.js";
import { announceFailure, announceReady } from "./ready.js";
import { createSlateServer } from "./server.js";

try {
  const config = readConfig(process.env);
  const server = createSlateServer(openStore(config), {
    webDir: config.webDir,
  });
  const url = await listen(server, config.port, "127.0.0.1");
  console.log(`Slate service listening on ${url}`);
  announceReady(url);
} catch (error) {
  console.error("The Slate service cannot start:", error);
  announceFailure((error as Error).message);
  process.exitCode = 1;
}
