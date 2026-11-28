import { readFileSync } from "node:fs";
import { type IncomingMessage, type ServerResponse } from "node:http";
import { join } from "node:path";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import { type CallToolResult } from "@modelcontextprotocol/sdk/types.js";
import {
  type Item,
  type ItemChange,
  ItemChangedError,
  SourceChangeError,
} from "./item.js";
import {
  type AgentOperationName,
  operations,
  type Store,
} from "./operations.js";

const {
  listItems,
  addItem,
  updateItem,
  moveItem,
  deleteItem,
  restoreItem,
  listItemChanges,
} = operations;

type RegisterTool = (server: McpServer, store: Store) => void;

const icon = {
  src: `data:image/png;base64,${readFileSync(join(import.meta.dirname, "../../icons/slate-64.png")).toString("base64")}`,
  mimeType: "image/png",
  sizes: ["64x64"],
};

const tools = {
  listItems: (server, store) =>
    server.registerTool(
      "list_items",
      {
        title: "List items",
        description:
          'List the items on the user\'s Slate list, their to-dos, tasks and priorities. Each item has an id, a name, a description (empty when it has none), sources (where the item came from, in the order they were added: links and references, each with an id and a label, or an empty list), a status (todo, in_progress, done, deferred, wont_do or delegated), a rank, the times it was created and last updated, and doneAt, the time it was marked done, or null when it is not done. With no input, it lists today\'s list: items that are todo, in_progress, or done today, in priority order, which is what each input defaults to. Each input narrows or orders the list on its own, so give every one that differs from its default. For example, every done item, newest first: statuses ["done"], doneTodayOnly false, orderBy doneAt. Every deleted item, most recently deleted first: deleted true, statuses ["todo", "in_progress", "done", "deferred", "wont_do", "delegated"], doneTodayOnly false, orderBy updatedAt. Whether something is already tracked, before you add it: search with a few distinctive words from it, across every item that is not deleted, current work first: {"search": "launch announcement", "statuses": ["todo", "in_progress", "done", "deferred", "wont_do", "delegated"], "doneTodayOnly": false, "orderBy": "status"}; an item that matches may be done or set aside, so check its status before you add another. When you have a link or reference the work came from, source is the better check, because it matches it exactly: source with the link, statuses of every status, doneTodayOnly false.',
        inputSchema: listItems.input,
      },
      (input) => reply(listItems.run(store, input)),
    ),
  addItem: (server, store) =>
    server.registerTool(
      "add_item",
      {
        title: "Add item",
        description:
          "Add a to-do, task or priority to the user's Slate list, their list for the day. Give it a description when there is context worth keeping with it, and sources when it came from somewhere the user may want to go back to, such as the Claude Desktop session, the line in a notes file, or the pull requests that it came from. To avoid tracking the same work twice, check first with list_items and a source. It goes to the top with the status todo; to put it lower down, move it afterwards.",
        inputSchema: addItem.input,
      },
      (input) => reply(addItem.run(store, input)),
    ),
  updateItem: (server, store) =>
    server.registerTool(
      "update_item",
      {
        title: "Update item",
        description:
          "Rename an item on the user's Slate list, change its description or sources, or change its status, such as marking a to-do done. Give at least one of name, description, addSources, removeSources, replaceSource and status. Sources change only by what you name: replaceSource changes one in place and removeSources removes some, each by the source's id from list_items or the last tool that returned the item, and addSources appends new ones, so you cannot remove a source by leaving it out, and you do not need to read the item's sources first unless you are removing or replacing one. Given together, they apply in that order: replaceSource, then removeSources, then addSources. Always give the item's latest updatedAt, from list_items or the last tool that returned the item: if the item has changed since, the update is refused so that a change you have not seen is not overwritten. A description replaces the old one, so to add to it, start from the description in list_items. A done item stays on today's list until the end of the day it was marked done; giving it todo or in_progress brings it back. Mark an item deferred when it is put off until later, wont_do when it will not be done, and delegated when it was handed to someone else, noting who in its description; these leave today's list straight away but keep their rank, and giving one todo, in_progress or done brings it back to its place.",
        inputSchema: updateItem.input,
      },
      (input) => replyIfUnchanged(() => updateItem.run(store, input), input.id),
    ),
  moveItem: (server, store) =>
    server.registerTool(
      "move_item",
      {
        title: "Move item",
        description:
          "Reprioritise an item on the user's Slate list by moving it to just before or just after another item, its neighbour, using ids from list_items. To make an item the most important, or put it at the top, move it before the first item; to make it the least important, move it after the last.",
        inputSchema: moveItem.input,
      },
      (input) =>
        replyIfFound(moveItem.run(store, input), [input.id, input.neighbourId]),
    ),
  deleteItem: (server, store) =>
    server.registerTool(
      "delete_item",
      {
        title: "Delete item",
        description:
          "Delete an item from the user's Slate list, such as one added by mistake or no longer wanted, using its id from list_items. To set aside finished work, or work that is deferred, delegated or will not be done, change its status with update_item instead. A deleted item leaves list_items unless you list deleted items, and restore_item puts it back.",
        inputSchema: deleteItem.input,
      },
      (input) => replyIfFound(deleteItem.run(store, input), [input.id]),
    ),
  restoreItem: (server, store) =>
    server.registerTool(
      "restore_item",
      {
        title: "Restore item",
        description:
          "Put an item deleted from the user's Slate list back at its earlier rank in the order, using its id from delete_item or from list_items with deleted. It keeps its status, so a restored item that is deferred, wont_do or delegated stays off today's list. An item that is not deleted is returned unchanged.",
        inputSchema: restoreItem.input,
      },
      (input) => replyIfFound(restoreItem.run(store, input), [input.id]),
    ),
  listItemChanges: (server, store) =>
    server.registerTool(
      "list_item_changes",
      {
        title: "List item changes",
        description:
          "List the changes to an item on the user's Slate list, newest first, using its id from list_items; a deleted item's too. Each change has at, the time it was made, actor, who made it: user in the Slate app or agent over MCP, kind, what changed: added, name, description, sources, status, rank (moved in the priority order), deleted or restored, and status, the new status when kind is status, otherwise null.",
        inputSchema: listItemChanges.input,
      },
      (input) => replyIfFound(listItemChanges.run(store, input), [input.id]),
    ),
} satisfies Record<AgentOperationName, RegisterTool>;

export function createSlateMcpServer(store: Store): McpServer {
  const server = new McpServer({
    name: "slate",
    version: "0.1.0",
    icons: [icon],
  });

  for (const register of Object.values(tools)) {
    register(server, store);
  }

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

  const server = createSlateMcpServer(store);
  const transport = new StreamableHTTPServerTransport({
    sessionIdGenerator: undefined,
    enableJsonResponse: true,
  });
  response.on("close", () => void server.close());

  await server.connect(transport);
  await transport.handleRequest(request, response);
}

function reply(value: Item | Item[] | ItemChange[]): CallToolResult {
  return { content: [{ type: "text", text: JSON.stringify(value) }] };
}

function replyIfFound(
  result: Item | ItemChange[] | undefined,
  ids: string[],
): CallToolResult {
  if (!result) {
    return replyError(
      `No item has the id ${ids.join(" or ")}. List the items to find the right id.`,
    );
  }
  return reply(result);
}

function replyIfUnchanged(
  update: () => Item | undefined,
  id: string,
): CallToolResult {
  try {
    return replyIfFound(update(), [id]);
  } catch (error) {
    if (error instanceof ItemChangedError) {
      return replyError(
        `The item ${id} has changed since you listed it, so it was not updated. List the items again to see the change before you update it.`,
      );
    }
    if (error instanceof SourceChangeError) {
      return replyError(
        `${error.message}, so it was not updated. List the items to see its sources.`,
      );
    }
    throw error;
  }
}

function replyError(text: string): CallToolResult {
  return { content: [{ type: "text", text }], isError: true };
}
