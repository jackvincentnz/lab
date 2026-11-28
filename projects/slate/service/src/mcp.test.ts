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
import { type Item, ItemChangedError, SourceChangeError } from "./item.js";
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
  await createSlateMcpServer(store, "http://127.0.0.1:5197").connect(
    serverTransport,
  );
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
    sources: [],
    status: "todo",
    rank: "a0",
    createdAt: "2026-11-22T09:00:00.000Z",
    updatedAt: "2026-11-22T09:00:00.000Z",
    doneAt: null,
  };
}

const sourceId = crypto.randomUUID();

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
      "get_item",
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

  it.each([
    ["add_item", "sources"],
    ["update_item", "addSources"],
  ])("shows the agent example sources on %s", async (name, property) => {
    const { tools } = await client.listTools();

    const tool = tools.find((candidate) => candidate.name === name);
    const { description } = tool?.inputSchema.properties?.[property] as {
      description: string;
    };
    expect(description).toContain('"text": "claude://');
    expect(description).toContain('"text": "~/notes/TODO.md:12"');
    expect(description).toContain(
      '"text": "~/notes/daily/2026-11-27.md#Follow-ups"',
    );
  });

  it("lists the store's items", async () => {
    const items = [anItem(), anItem()];
    store.list.mockReturnValue(items);

    const result = await call("list_items");

    expect(result.content).toEqual(text(items));
  });

  it("lists every item that matches the given query", async () => {
    const items = [anItem()];
    store.list.mockReturnValue(items);

    const result = await call("list_items", {
      statuses: ["done"],
      doneTodayOnly: false,
      deleted: true,
      orderBy: "updatedAt",
    });

    expect(store.list).toHaveBeenCalledWith({
      statuses: ["done"],
      doneTodayOnly: false,
      deleted: true,
      orderBy: "updatedAt",
    });
    expect(result.content).toEqual(text(items));
  });

  it("searches with a trimmed query", async () => {
    store.list.mockReturnValue([]);

    await call("list_items", { search: "  domain name ", orderBy: "status" });

    expect(store.list).toHaveBeenCalledWith(
      expect.objectContaining({ search: "domain name", orderBy: "status" }),
    );
  });

  it("refuses a blank search", async () => {
    const result = await call("list_items", { search: "  " });

    expect(result.isError).toBe(true);
    expect(store.list).not.toHaveBeenCalled();
  });

  it("lists today's list when given no query", async () => {
    store.list.mockReturnValue([]);

    await call("list_items");

    expect(store.list).toHaveBeenCalledWith({
      statuses: ["todo", "in_progress", "done"],
      doneTodayOnly: true,
      deleted: false,
      orderBy: "rank",
    });
  });

  it("refuses the old doing status to list by", async () => {
    const result = await call("list_items", { statuses: ["doing"] });

    expect(result.isError).toBe(true);
    expect(store.list).not.toHaveBeenCalled();
  });

  it("refuses an unknown status to list by", async () => {
    const result = await call("list_items", { statuses: ["blocked"] });

    expect(result.isError).toBe(true);
    expect(store.list).not.toHaveBeenCalled();
  });

  it.each(["in_progress", "deferred", "wont_do", "delegated"] as const)(
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

  it("refuses the old doing status for an update without reaching the store", async () => {
    const item = anItem();

    const result = await call("update_item", {
      id: item.id,
      status: "doing",
      updatedAt: item.updatedAt,
    });

    expect(result.isError).toBe(true);
    expect(store.update).not.toHaveBeenCalled();
  });

  it("adds an item with its trimmed name", async () => {
    const item = anItem();
    store.add.mockReturnValue(item);

    const result = await call("add_item", { name: "  Write the plan " });

    expect(store.add).toHaveBeenCalledWith("Write the plan", "", []);
    expect(result.content).toEqual(text(item));
  });

  it("adds an item with its description", async () => {
    store.add.mockReturnValue(anItem());

    await call("add_item", { name: "Write the plan", description: "Why" });

    expect(store.add).toHaveBeenCalledWith("Write the plan", "Why", []);
  });

  it.each([
    { kind: "link", url: "https://example.com/pull/1", label: "Pull request" },
    {
      kind: "reference",
      text: "claude://example/session",
      label: "Claude Desktop session",
    },
  ])("adds an item with its source: %o", async (source) => {
    store.add.mockReturnValue(anItem());

    await call("add_item", { name: "Write the plan", sources: [source] });

    expect(store.add).toHaveBeenCalledWith("Write the plan", "", [source]);
  });

  it("refuses a reference on more than one line", async () => {
    const result = await call("add_item", {
      name: "Write the plan",
      sources: [
        { kind: "reference", text: "notes.md\nplan.md", label: "Notes" },
      ],
    });

    expect(result).toMatchObject({
      isError: true,
      content: [
        { text: expect.stringContaining("Give a reference on one line") },
      ],
    });
  });

  it("refuses a source link that is not https", async () => {
    const result = await call("add_item", {
      name: "Write the plan",
      sources: [{ kind: "link", url: "javascript:alert(1)", label: "Run" }],
    });

    expect(result).toMatchObject({
      isError: true,
      content: [
        {
          text: expect.stringContaining(
            "Give a link that starts with https://",
          ),
        },
      ],
    });
    expect(store.add).not.toHaveBeenCalled();
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

  it.each([
    {
      addSources: [
        { kind: "link", url: "https://example.com/pull/1", label: "PR" },
      ],
    },
    { removeSources: [sourceId] },
    {
      replaceSource: {
        id: sourceId,
        to: { kind: "reference", text: "notes/TODO.md#Later", label: "TODO" },
      },
    },
  ])("changes an item's sources: %o", async (change) => {
    const item = anItem();
    store.update.mockReturnValue(item);

    await call("update_item", {
      id: item.id,
      ...change,
      updatedAt: item.updatedAt,
    });

    expect(store.update).toHaveBeenCalledWith({
      id: item.id,
      ...change,
      updatedAt: item.updatedAt,
    });
  });

  it("tells the agent when the source to replace is not on the item", async () => {
    const item = anItem();
    store.update.mockImplementation(() => {
      throw new SourceChangeError(
        `The item has no source with the id ${sourceId}`,
      );
    });

    const result = await call("update_item", {
      id: item.id,
      replaceSource: {
        id: sourceId,
        to: { kind: "reference", text: "notes/plan.md", label: "Plan" },
      },
      updatedAt: item.updatedAt,
    });

    expect(result).toMatchObject({
      isError: true,
      content: [
        {
          text: `The item has no source with the id ${sourceId}, so it was not updated. List the items to see its sources.`,
        },
      ],
    });
  });

  it("lists the items with a source", async () => {
    store.list.mockReturnValue([]);

    await call("list_items", { source: "https://example.com/pull/1" });

    expect(store.list).toHaveBeenCalledWith(
      expect.objectContaining({ source: "https://example.com/pull/1" }),
    );
  });

  it("tells the agent that an update keeps the sources it does not name", async () => {
    const { tools } = await client.listTools();

    const update = tools.find((tool) => tool.name === "update_item");
    expect(update?.description).toContain(
      "cannot remove a source by leaving it out",
    );
    expect(update?.inputSchema.properties).not.toHaveProperty("sources");
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

describe("get_item", () => {
  it("gets an item by its id, with a link that opens it", async () => {
    const found = anItem();
    store.list.mockReturnValue([anItem(), found]);

    const result = await call("get_item", { item: found.id });

    expect(result.content).toEqual(
      text({
        ...found,
        deleted: false,
        link: `http://127.0.0.1:5197/?item=${found.id}`,
      }),
    );
    expect(store.list).toHaveBeenCalledWith({
      statuses: [
        "todo",
        "in_progress",
        "done",
        "deferred",
        "wont_do",
        "delegated",
      ],
      doneTodayOnly: false,
    });
  });

  it("gets the item a link opens", async () => {
    const found = anItem();
    store.list.mockReturnValue([found]);

    const result = await call("get_item", {
      item: `http://127.0.0.1:5197/?item=${found.id}`,
    });

    expect(result.content).toEqual(
      text({
        ...found,
        deleted: false,
        link: `http://127.0.0.1:5197/?item=${found.id}`,
      }),
    );
  });

  it("gets a deleted item and says it is deleted", async () => {
    const found = anItem();
    store.list.mockImplementation((query) =>
      query?.deleted ? [found] : [anItem()],
    );

    const result = await call("get_item", { item: found.id });

    expect(result.content).toEqual(
      text({
        ...found,
        deleted: true,
        link: `http://127.0.0.1:5197/?item=${found.id}`,
      }),
    );
  });

  it("names the id or link that finds no item", async () => {
    store.list.mockReturnValue([anItem()]);

    const result = await call("get_item", { item: "not-an-item" });

    expect(result.isError).toBe(true);
    expect(result.content).toEqual([
      {
        type: "text",
        text: "No item has the id or link not-an-item. Find the item with list_items and a search instead.",
      },
    ]);
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
