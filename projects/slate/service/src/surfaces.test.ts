import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { InMemoryTransport } from "@modelcontextprotocol/sdk/inMemory.js";
import { expect, it, vi } from "vitest";
import { createSlateMcpServer } from "./mcp.js";
import { agentOnly, webOnly } from "./operations.js";
import { slateRouter } from "./router.js";

const webOnlyNames = new Set<string>(webOnly);

function toolName(procedure: string): string {
  return procedure.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`);
}

async function listToolNames(): Promise<string[]> {
  const store = {
    list: vi.fn(),
    add: vi.fn(),
    update: vi.fn(),
    move: vi.fn(),
    delete: vi.fn(),
    restore: vi.fn(),
  };
  const [clientTransport, serverTransport] =
    InMemoryTransport.createLinkedPair();
  await createSlateMcpServer(store).connect(serverTransport);
  const client = new Client({ name: "test", version: "1.0.0" });
  await client.connect(clientTransport);

  const { tools } = await client.listTools();

  await client.close();
  return tools.map((tool) => tool.name);
}

it("offers every operation to both the web UI and agents", async () => {
  const procedures = Object.keys(slateRouter._def.procedures);

  const tools = await listToolNames();

  expect(tools.sort()).toEqual(
    [
      ...procedures.filter((procedure) => !webOnlyNames.has(procedure)),
      ...agentOnly,
    ]
      .map(toolName)
      .sort(),
  );
});
