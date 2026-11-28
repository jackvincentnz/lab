import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { forward } from "./forward.js";

const slateUrl = process.env["SLATE_MCP_URL"];
if (!slateUrl) {
  throw new Error("Set SLATE_MCP_URL to Slate's MCP endpoint.");
}

await forward(
  new StdioServerTransport(),
  new StreamableHTTPClientTransport(new URL(slateUrl)),
  slateUrl,
);
