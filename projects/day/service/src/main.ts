import { readConfig } from "./config.js";
import { createDayServer } from "./server.js";
import { openItemStore } from "./store.js";

const { dataDir, port } = readConfig(process.env);

createDayServer(openItemStore(dataDir)).listen(port, "127.0.0.1", () => {
  console.log(`Day service listening on http://127.0.0.1:${port}`);
});
