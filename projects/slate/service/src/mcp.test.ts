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
import { type Item, ItemChangedError } from "./item.js";
import { createSlateMcpServer, handleMcpRequest } from "./mcp.js";
import { type Store } from "./operations.js";

let store: { [K in keyof Store]: Mock<Store[K]> };
let client: Client;

beforeEach(async () => {
  store = {
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
    description: "",
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

describe("createSlateMcpServer", () => {
  it("sends Slate's icon", () => {
    const icons = client.getServerVersion()?.icons;

    expect(icons).toEqual([
      {
        src: expect.stringMatching(/^data:image\/png;base64,iVBORw0KGgo/),
        mimeType: "image/png",
        sizes: ["64x64"],
      },
    ]);
  });

  it("offers a tool for each operation", async () => {
    const { tools } = await client.listTools();

    expect(tools.map((tool) => tool.name)).toEqual([
      "list_items",
      "add_item",
      "update_item",
      "move_item",
      "delete_item",
      "restore_item",
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

  it("lists the items with the given status", async () => {
    const items = [{ ...anItem(), status: "delegated" as const }];
    store.list.mockReturnValue(items);

    const result = await call("list_items", { status: "delegated" });

    expect(store.list).toHaveBeenCalledWith("delegated");
    expect(result.content).toEqual(text(items));
  });

  it("refuses an unknown status to list by", async () => {
    const result = await call("list_items", { status: "blocked" });

    expect(result.isError).toBe(true);
    expect(store.list).not.toHaveBeenCalled();
  });

  it.each(["deferred", "wont_do", "delegated"] as const)(
    "sets an item %s",
    async (status) => {
      const item = anItem();
      store.update.mockReturnValue({ ...item, status });

      await call("update_item", {
        id: item.id,
        status,
        updatedAt: item.updatedAt,
      });

      expect(store.update).toHaveBeenCalledWith({
        id: item.id,
        status,
        updatedAt: item.updatedAt,
      });
    },
  );

  it("adds an item with its trimmed name", async () => {
    const item = anItem();
    store.add.mockReturnValue(item);

    const result = await call("add_item", { name: "  Write the plan " });

    expect(store.add).toHaveBeenCalledWith("Write the plan", "");
    expect(result.content).toEqual(text(item));
  });

  it("adds an item with its description", async () => {
    store.add.mockReturnValue(anItem());

    await call("add_item", { name: "Write the plan", description: "Why" });

    expect(store.add).toHaveBeenCalledWith("Write the plan", "Why");
  });

  it("refuses invalid input without reaching the store", async () => {
    const result = await call("add_item", { name: "  " });

    expect(result.isError).toBe(true);
    expect(store.add).not.toHaveBeenCalled();
  });

  it("returns the updated item", async () => {
    const item = anItem();
    store.update.mockReturnValue(item);

    const result = await call("update_item", {
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    });

    expect(result.content).toEqual(text(item));
  });

  it("requires the updatedAt that guards an update", async () => {
    const { tools } = await client.listTools();

    const update = tools.find((tool) => tool.name === "update_item");
    expect(update?.inputSchema).toMatchObject({
      properties: {
        updatedAt: { description: expect.stringContaining("list_items") },
      },
      required: expect.arrayContaining(["updatedAt"]),
    });
  });

  it("tells the agent where to find a missing updatedAt", async () => {
    const result = await call("update_item", {
      id: crypto.randomUUID(),
      status: "done",
    });

    expect(result).toMatchObject({
      isError: true,
      content: [
        {
          text: expect.stringContaining(
            "Give the item's updatedAt from list_items or the last tool that returned the item",
          ),
        },
      ],
    });
    expect(store.update).not.toHaveBeenCalled();
  });

  it("tells the agent to list again when the item changed since", async () => {
    const item = anItem();
    store.update.mockImplementation(() => {
      throw new ItemChangedError(item.id);
    });

    const result = await call("update_item", {
      id: item.id,
      status: "done",
      updatedAt: item.updatedAt,
    });

    expect(result).toMatchObject({
      isError: true,
      content: [
        {
          text: `The item ${item.id} has changed since you listed it, so it was not updated. List the items again to see the change before you update it.`,
        },
      ],
    });
  });

  it("changes an item's description", async () => {
    const item = anItem();
    store.update.mockReturnValue(item);

    await call("update_item", {
      id: item.id,
      description: "New notes",
      updatedAt: item.updatedAt,
    });

    expect(store.update).toHaveBeenCalledWith({
      id: item.id,
      description: "New notes",
      updatedAt: item.updatedAt,
    });
  });

  it("names the unknown id on update", async () => {
    const id = crypto.randomUUID();

    const result = await call("update_item", {
      id,
      status: "done",
      updatedAt: "2026-11-22T09:00:00.000Z",
    });

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

  it("returns the deleted item", async () => {
    const item = anItem();
    store.delete.mockReturnValue(item);

    const result = await call("delete_item", { id: item.id });

    expect(store.delete).toHaveBeenCalledWith(item.id);
    expect(result.content).toEqual(text(item));
  });

  it("names the unknown id on delete", async () => {
    const id = crypto.randomUUID();

    const result = await call("delete_item", { id });

    expect(result).toMatchObject({
      isError: true,
      content: [{ text: expect.stringContaining(id) }],
    });
  });

  it("returns the restored item", async () => {
    const item = anItem();
    store.restore.mockReturnValue(item);

    const result = await call("restore_item", { id: item.id });

    expect(store.restore).toHaveBeenCalledWith(item.id);
    expect(result.content).toEqual(text(item));
  });

  it("names the unknown id on restore", async () => {
    const id = crypto.randomUUID();

    const result = await call("restore_item", { id });

    expect(result).toMatchObject({
      isError: true,
      content: [{ text: expect.stringContaining(id) }],
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
});
