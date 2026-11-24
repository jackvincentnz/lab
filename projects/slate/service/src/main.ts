import { type AddressInfo } from "node:net";
import { readConfig } from "./config.js";
import { announceReady } from "./ready.js";
import { createSlateServer } from "./server.js";
import { openItemStore } from "./store.js";

const { dataDir, port, webDir } = readConfig(process.env);

const server = createSlateServer(openItemStore(dataDir), { webDir });

server.listen(port, "127.0.0.1", () => {
  const { address, port: boundPort } = server.address() as AddressInfo;
  const url = `http://${address}:${boundPort}`;
  console.log(`Slate service listening on ${url}`);
  announceReady(url);
});
