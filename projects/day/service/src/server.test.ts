import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { request, type IncomingHttpHeaders, type Server } from "node:http";
import { type AddressInfo } from "node:net";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createDayServer, type DayServerOptions } from "./server.js";
import { type Item, openItemStore } from "./store.js";

let folder: string;
let webDir: string;
let server: Server;

beforeEach(() => {
  folder = mkdtempSync(join(tmpdir(), "day-server-"));
  webDir = join(folder, "web");
  mkdirSync(join(webDir, "assets"), { recursive: true });
  writeFileSync(join(webDir, "index.html"), "<h1>Day</h1>");
  writeFileSync(join(webDir, "assets", "index.js"), "render();");
  writeFileSync(join(folder, "secret"), "private");
});

afterEach(async () => {
  await new Promise((resolve) => server.close(resolve));
  rmSync(folder, { recursive: true, force: true });
});

async function start(options: DayServerOptions = {}): Promise<void> {
  server = createDayServer(openItemStore(folder), options);
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
}

interface Response {
  status: number;
  headers: IncomingHttpHeaders;
  body: string;
}

function send(
  path: string,
  host = "localhost:5197",
  body?: string,
): Promise<Response> {
  const { port } = server.address() as AddressInfo;
  return new Promise((resolve, reject) => {
    request(
      {
        host: "127.0.0.1",
        port,
        path,
        method: body === undefined ? "GET" : "POST",
        headers: { host, "content-type": "application/json" },
      },
      (response) => {
        let body = "";
        response.setEncoding("utf8");
        response.on("data", (chunk: string) => (body += chunk));
        response.on("end", () =>
          resolve({
            status: response.statusCode ?? 0,
            headers: response.headers,
            body,
          }),
        );
      },
    )
      .on("error", reject)
      .end(body);
  });
}

async function connectMcpClient(): Promise<Client> {
  const { port } = server.address() as AddressInfo;
  const client = new Client({ name: "test", version: "1.0.0" });
  await client.connect(
    new StreamableHTTPClientTransport(new URL(`http://127.0.0.1:${port}/mcp`)),
  );
  return client;
}

async function addItemOverMcp(client: Client, name: string): Promise<Item> {
  const result = await client.callTool({
    name: "add_item",
    arguments: { name },
  });
  const [{ text }] = result.content as [{ text: string }];
  return JSON.parse(text) as Item;
}

describe("createDayServer", () => {
  it("serves the router under the API path to a loopback host", async () => {
    await start();
    await send("/api/addItem", undefined, '{"name":"Plan the week"}');

    const response = await send("/api/listItems");

    expect(response.status).toBe(200);
    expect(JSON.parse(response.body)).toMatchObject({
      result: { data: [{ name: "Plan the week", status: "todo" }] },
    });
  });

  it("refuses a request for another host", async () => {
    await start();

    const response = await send("/api/listItems", "attacker.example:5197");

    expect(response.status).toBe(403);
  });

  it("lets an MCP client change the list that the API serves", async () => {
    await start();
    const client = await connectMcpClient();

    const plan = await addItemOverMcp(client, "Plan the week");
    const venue = await addItemOverMcp(client, "Book the venue");
    await client.callTool({
      name: "move_item",
      arguments: { id: venue.id, neighbourId: plan.id, position: "before" },
    });
    await client.close();

    const response = await send("/api/listItems");
    expect(JSON.parse(response.body)).toMatchObject({
      result: { data: [{ name: "Book the venue" }, { name: "Plan the week" }] },
    });
  });

  it("serves the web UI's page at the root", async () => {
    await start({ webDir });

    const response = await send("/");

    expect(response).toMatchObject({
      status: 200,
      headers: { "content-type": "text/html; charset=utf-8" },
      body: "<h1>Day</h1>",
    });
  });

  it("serves the web UI's assets", async () => {
    await start({ webDir });

    const response = await send("/assets/index.js");

    expect(response).toMatchObject({
      status: 200,
      headers: { "content-type": "text/javascript; charset=utf-8" },
      body: "render();",
    });
  });

  it("sends the content security policy", async () => {
    await start({ webDir });

    const response = await send("/");

    expect(response.headers["content-security-policy"]).toBe(
      "default-src 'self'; style-src 'self' 'unsafe-inline'",
    );
  });

  it("answers a missing file with not found", async () => {
    await start({ webDir });

    const response = await send("/assets/missing.js");

    expect(response.status).toBe(404);
  });

  it("refuses a path outside the web UI folder", async () => {
    await start({ webDir });

    const response = await send("/..%2fsecret");

    expect(response.status).toBe(404);
  });

  it("answers a malformed path with not found", async () => {
    await start({ webDir });

    const response = await send("/%E0");

    expect(response.status).toBe(404);
  });

  it("answers a request target that is not a URL with bad request", async () => {
    await start({ webDir });

    const response = await send("//a:b@");

    expect(response.status).toBe(400);
  });

  it("answers the root with not found without a web UI folder", async () => {
    await start();

    const response = await send("/");

    expect(response.status).toBe(404);
  });
});
