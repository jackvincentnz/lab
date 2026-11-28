import { mkdtempSync, readFileSync, rmSync } from "node:fs";
import { type Server } from "node:http";
import { type AddressInfo } from "node:net";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { unpackExtension, validateManifest } from "@anthropic-ai/mcpb";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createSlateServer } from "../../service/src/server.js";
import { openItemStore } from "../../service/src/store.js";

let folder: string;
let extensionDir: string;
let slate: Server | undefined;
let client: Client | undefined;

beforeEach(() => {
  folder = mkdtempSync(join(tmpdir(), "slate-extension-"));
  extensionDir = join(folder, "extension");
});

afterEach(async () => {
  await client?.close();
  client = undefined;
  if (slate) {
    const server = slate;
    server.closeAllConnections();
    await new Promise((resolve) => server.close(resolve));
    slate = undefined;
  }
  rmSync(folder, { recursive: true, force: true });
});

async function unpack(bundle: string): Promise<boolean> {
  return unpackExtension({
    mcpbPath: join(import.meta.dirname, "..", bundle),
    outputDir: extensionDir,
    silent: true,
  });
}

async function startSlate(): Promise<string> {
  const server = createSlateServer(openItemStore(join(folder, "data")));
  slate = server;
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address() as AddressInfo;
  return `http://127.0.0.1:${port}/mcp`;
}

function launch(slateUrl: string): StdioClientTransport {
  const manifest = JSON.parse(
    readFileSync(join(extensionDir, "manifest.json"), "utf8"),
  ) as { server: { mcp_config: { args: string[] } } };
  return new StdioClientTransport({
    command: process.execPath,
    args: manifest.server.mcp_config.args.map((arg) =>
      arg.replaceAll("${__dirname}", extensionDir),
    ),
    env: {
      ...(process.env as Record<string, string>),
      SLATE_MCP_URL: slateUrl,
    },
  });
}

describe.each(["slate.mcpb", "slate-dev.mcpb"])("%s", (bundle) => {
  it("unpacks to a valid extension", async () => {
    const unpacked = await unpack(bundle);

    expect(unpacked).toBe(true);
    expect(validateManifest(join(extensionDir, "manifest.json"))).toBe(true);
  });

  it("connects Claude to Slate", async () => {
    await unpack(bundle);
    const slateUrl = await startSlate();
    client = new Client({ name: "claude", version: "1.0.0" });

    await client.connect(launch(slateUrl));
    const { tools } = await client.listTools();

    expect(tools.map(({ name }) => name)).toContain("list_items");
  });
});
