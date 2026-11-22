import { type AddressInfo } from "node:net";
import { readConfig } from "./config.js";
import { createDayServer } from "./server.js";
import { openItemStore } from "./store.js";

const { dataDir, port } = readConfig(process.env);

const server = createDayServer(openItemStore(dataDir));

server.listen(port, "127.0.0.1", () => {
  const { address, port: boundPort } = server.address() as AddressInfo;
  console.log(`Day service listening on http://${address}:${boundPort}`);
});
