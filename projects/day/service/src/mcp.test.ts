import { type IncomingMessage, type ServerResponse } from "node:http";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { InMemoryTransport } from "@modelcontextprotocol/sdk/inMemory.js";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  type Mock,
  vi,
} from "vitest";
import { createDayMcpServer, handleMcpRequest } from "./mcp.js";
import { type Context } from "./router.js";
import { type Item } from "./store.js";

type Store = Context["store"];

let store: { [K in keyof Store]: Mock<Store[K]> };
let client: Client;

beforeEach(async () => {
  store = { list: vi.fn(), add: vi.fn(), update: vi.fn(), move: vi.fn() };
  const [clientTransport, serverTransport] =
    InMemoryTransport.createLinkedPair();
  await createDayMcpServer(store).connect(serverTransport);
  client = new Client({ name: "test", version: "1.0.0" });
  await client.connect(clientTransport);
});

afterEach(async () => {
  await client.close();
});

function anItem(): Item {
  return {
    id: crypto.randomUUID(),
    name: "Write the plan",
    status: "todo",
    rank: "a0",
    createdAt: "2026-11-22T09:00:00.000Z",
    updatedAt: "2026-11-22T09:00:00.000Z",
  };
}

function call(name: string, args: Record<string, unknown> = {}) {
  return client.callTool({ name, arguments: args });
}

function text(value: unknown) {
  return [{ type: "text", text: JSON.stringify(value) }];
}

describe("createDayMcpServer", () => {
  it("offers a tool for each operation", async () => {
    const { tools } = await client.listTools();

    expect(tools.map((tool) => tool.name)).toEqual([
      "list_items",
      "add_item",
      "update_item",
      "move_item",
    ]);
  });

  it("describes each input field to the agent", async () => {
    const { tools } = await client.listTools();

    const move = tools.find((tool) => tool.name === "move_item");
    expect(move?.inputSchema.properties).toMatchObject({
      id: { description: expect.any(String) },
      neighbourId: { description: expect.any(String) },
      position: { description: expect.any(String) },
    });
  });

  it("lists the store's items", async () => {
    const items = [anItem(), anItem()];
    store.list.mockReturnValue(items);

    const result = await call("list_items");

    expect(result.content).toEqual(text(items));
  });

  it("adds an item with its trimmed name", async () => {
    const item = anItem();
    store.add.mockReturnValue(item);

    const result = await call("add_item", { name: "  Write the plan " });

    expect(store.add).toHaveBeenCalledWith("Write the plan");
    expect(result.content).toEqual(text(item));
  });

  it("refuses invalid input without reaching the store", async () => {
    const result = await call("add_item", { name: "  " });

    expect(result.isError).toBe(true);
    expect(store.add).not.toHaveBeenCalled();
  });

  it("returns the updated item", async () => {
    const item = anItem();
    store.update.mockReturnValue(item);

    const result = await call("update_item", { id: item.id, status: "done" });

    expect(result.content).toEqual(text(item));
  });

  it("names the unknown id on update", async () => {
    const id = crypto.randomUUID();

    const result = await call("update_item", { id, status: "done" });

    expect(result).toMatchObject({
      isError: true,
      content: [{ text: expect.stringContaining(id) }],
    });
  });

  it("returns the moved item", async () => {
    const item = anItem();
    store.move.mockReturnValue(item);

    const result = await call("move_item", {
      id: item.id,
      neighbourId: crypto.randomUUID(),
      position: "before",
    });

    expect(result.content).toEqual(text(item));
  });

  it("names both ids when a move finds no item", async () => {
    const id = crypto.randomUUID();
    const neighbourId = crypto.randomUUID();

    const result = await call("move_item", {
      id,
      neighbourId,
      position: "after",
    });

    expect(result).toMatchObject({
      isError: true,
      content: [
        {
          text: `No item has the id ${id} or ${neighbourId}. List the items to find the right id.`,
        },
      ],
    });
  });
});

describe("handleMcpRequest", () => {
  function respond(method: string, headers: IncomingMessage["headers"]) {
    const response = { writeHead: vi.fn(), end: vi.fn() };
    response.writeHead.mockReturnValue(response);
    const request = { method, headers } as IncomingMessage;

    void handleMcpRequest(
      store,
      request,
      response as unknown as ServerResponse,
    );

    return response;
  }

  it("answers a stream request with method not allowed", () => {
    const response = respond("GET", { host: "127.0.0.1:5197" });

    expect(response.writeHead).toHaveBeenCalledWith(405, { allow: "POST" });
  });

  it("refuses a request from another site's page", () => {
    const response = respond("POST", {
      host: "127.0.0.1:5197",
      origin: "https://attacker.example",
    });

    expect(response.writeHead).toHaveBeenCalledWith(403);
  });
});
