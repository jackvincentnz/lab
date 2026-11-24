import { type IncomingMessage, type ServerResponse } from "node:http";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import { type CallToolResult } from "@modelcontextprotocol/sdk/types.js";
import { isServiceOrigin } from "./host.js";
import { type Context } from "./router.js";
import { addItemInput, moveItemInput, updateItemInput } from "./schemas.js";
import { type Item } from "./store.js";

type Store = Context["store"];

export function createSlateMcpServer(store: Store): McpServer {
  const server = new McpServer({ name: "slate", version: "0.1.0" });

  server.registerTool(
    "list_items",
    {
      title: "List items",
      description:
        "List the items on the user's Slate list, their to-dos, tasks and priorities for the day, in priority order with the most important first. Each item has an id, a name, a status (todo, doing or done), a rank, and the times it was created and last updated.",
    },
    () => reply(store.list()),
  );

  server.registerTool(
    "add_item",
    {
      title: "Add item",
      description:
        "Add a to-do, task or priority to the user's Slate list, their list for the day. It goes to the bottom with the status todo; to put it at the top or higher up, move it afterwards.",
      inputSchema: addItemInput,
    },
    ({ name }) => reply(store.add(name)),
  );

  server.registerTool(
    "update_item",
    {
      title: "Update item",
      description:
        "Rename an item on the user's Slate list or change its status, such as marking a to-do done. Give at least one of name and status.",
      inputSchema: updateItemInput,
    },
    (input) => replyIfFound(store.update(input), [input.id]),
  );

  server.registerTool(
    "move_item",
    {
      title: "Move item",
      description:
        "Reprioritise an item on the user's Slate list by moving it to just before or just after another item, its neighbour, using ids from list_items. To make an item the most important, or put it at the top, move it before the first item; to make it the least important, move it after the last.",
      inputSchema: moveItemInput,
    },
    (input) => replyIfFound(store.move(input), [input.id, input.neighbourId]),
  );

  return server;
}

export async function handleMcpRequest(
  store: Store,
  request: IncomingMessage,
  response: ServerResponse,
): Promise<void> {
  // Without sessions there is nothing to stream on a GET or to end on a DELETE.
  if (request.method !== "POST") {
    response.writeHead(405, { allow: "POST" }).end();
    return;
  }
  // The MCP specification requires refusing a web page from another origin, which can reach a
  // loopback host.
  if (!isServiceOrigin(request.headers.origin, request.headers.host)) {
    response.writeHead(403).end();
    return;
  }

  const server = createSlateMcpServer(store);
  const transport = new StreamableHTTPServerTransport({
    sessionIdGenerator: undefined,
    enableJsonResponse: true,
  });
  response.on("close", () => void server.close());

  await server.connect(transport);
  await transport.handleRequest(request, response);
}

function reply(value: Item | Item[]): CallToolResult {
  return { content: [{ type: "text", text: JSON.stringify(value) }] };
}

function replyIfFound(item: Item | undefined, ids: string[]): CallToolResult {
  if (!item) {
    return {
      content: [
        {
          type: "text",
          text: `No item has the id ${ids.join(" or ")}. List the items to find the right id.`,
        },
      ],
      isError: true,
    };
  }
  return reply(item);
}
