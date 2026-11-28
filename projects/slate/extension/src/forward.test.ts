import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { InMemoryTransport } from "@modelcontextprotocol/sdk/inMemory.js";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { LATEST_PROTOCOL_VERSION } from "@modelcontextprotocol/sdk/types.js";
import { describe, expect, it, vi } from "vitest";
import { forward } from "./forward.js";

const slateUrl = () =>
  `http://127.0.0.1:${5000 + Math.floor(Math.random() * 1000)}/mcp`;

async function connect(slate: InMemoryTransport, url = slateUrl()) {
  const [claude, proxy] = InMemoryTransport.createLinkedPair();
  await forward(proxy, slate, url);
  const client = new Client({ name: "claude", version: "1.0.0" });
  await client.connect(claude);
  return client;
}

describe("forward", () => {
  it("forwards Claude's requests to Slate and Slate's answers to Claude", async () => {
    const [proxy, slate] = InMemoryTransport.createLinkedPair();
    const server = new McpServer({ name: "slate", version: "1.0.0" });
    const toolName = `tool_${Math.floor(Math.random() * 1000)}`;
    server.registerTool(toolName, {}, () => ({ content: [] }));
    await server.connect(slate);

    const client = await connect(proxy);

    const { tools } = await client.listTools();
    expect(tools.map(({ name }) => name)).toEqual([toolName]);
  });

  it("tells Slate the protocol version that initialize agreed", async () => {
    const [proxy, slate] = InMemoryTransport.createLinkedPair();
    await new McpServer({ name: "slate", version: "1.0.0" }).connect(slate);
    const setProtocolVersion = vi.fn();

    await connect(Object.assign(proxy, { setProtocolVersion }));

    expect(setProtocolVersion).toHaveBeenCalledWith(LATEST_PROTOCOL_VERSION);
  });

  it("answers Claude with an error when Slate does not answer", async () => {
    const [proxy] = InMemoryTransport.createLinkedPair();
    proxy.send = () => Promise.reject(new Error("fetch failed"));
    const url = slateUrl();

    const connecting = connect(proxy, url);

    await expect(connecting).rejects.toThrow(
      `Slate did not answer at ${url}. Check that Slate is running.`,
    );
  });
});
