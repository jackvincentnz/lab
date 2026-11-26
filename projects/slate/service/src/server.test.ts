import { mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import {
  request,
  type IncomingHttpHeaders,
  type OutgoingHttpHeaders,
  type Server,
} from "node:http";
import { type AddressInfo } from "node:net";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { type Item } from "./item.js";
import { createSlateServer, type SlateServerOptions } from "./server.js";
import { openItemStore } from "./store.js";

let folder: string;
let webDir: string;
let server: Server;

beforeEach(() => {
  folder = mkdtempSync(join(tmpdir(), "slate-server-"));
  webDir = join(folder, "web");
  mkdirSync(join(webDir, "assets"), { recursive: true });
  writeFileSync(join(webDir, "index.html"), "<h1>Slate</h1>");
  writeFileSync(join(webDir, "assets", "index.js"), "render();");
  writeFileSync(join(folder, "secret"), "private");
});

afterEach(async () => {
  await new Promise((resolve) => server.close(resolve));
  rmSync(folder, { recursive: true, force: true });
});

async function start(options: SlateServerOptions = {}): Promise<void> {
  server = createSlateServer(openItemStore(folder), options);
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
}

interface Response {
  status: number;
  headers: IncomingHttpHeaders;
  body: string;
}

interface Request {
  body?: string;
  headers?: OutgoingHttpHeaders;
}

function send(
  path: string,
  { body, headers }: Request = {},
): Promise<Response> {
  const { port } = server.address() as AddressInfo;
  return new Promise((resolve, reject) => {
    request(
      {
        host: "127.0.0.1",
        port,
        path,
        method: body === undefined ? "GET" : "POST",
        headers: {
          host: "localhost:5197",
          "content-type": "application/json",
          ...headers,
        },
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

function anotherSite(): string {
  return `https://${crypto.randomUUID()}.example`;
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

describe("createSlateServer", () => {
  it("serves the router under the API path to a loopback host", async () => {
    await start();
    await send("/api/addItem", { body: '{"name":"Plan the week"}' });

    const response = await send("/api/listItems");

    expect(response.status).toBe(200);
    expect(JSON.parse(response.body)).toMatchObject({
      result: { data: [{ name: "Plan the week", status: "todo" }] },
    });
  });

  it("logs a failed API request once with its procedure", async () => {
    const logError = vi
      .spyOn(console, "error")
      .mockImplementation(() => undefined);
    await start();
    const procedure = `missing${crypto.randomUUID().slice(0, 8)}`;

    const response = await send(`/api/${procedure}`);

    expect(response.status).toBe(404);
    expect(logError).toHaveBeenCalledOnce();
    expect(logError.mock.calls[0]?.[0]).toBe(
      `The API request to ${procedure} failed:`,
    );
  });

  it("refuses a request for another host", async () => {
    await start();

    const response = await send("/api/listItems", {
      headers: { host: "attacker.example:5197" },
    });

    expect(response.status).toBe(403);
  });

  it("refuses a form posted from another site's page", async () => {
    await start();
    const boundary = crypto.randomUUID();

    const response = await send("/api/addItem", {
      body: `--${boundary}\r\ncontent-disposition: form-data; name="name"\r\n\r\n${crypto.randomUUID()}\r\n--${boundary}--\r\n`,
      headers: {
        origin: anotherSite(),
        "content-type": `multipart/form-data; boundary=${boundary}`,
      },
    });

    expect(response.status).toBe(403);
  });

  it("refuses a write from another site's page", async () => {
    await start();

    const response = await send("/api/addItem", {
      body: JSON.stringify({ name: crypto.randomUUID() }),
      headers: { origin: anotherSite() },
    });

    expect(response.status).toBe(403);
  });

  it.each(["localhost:5197", "127.0.0.1:5196"])(
    "accepts a write from the page served at %s",
    async (host) => {
      await start();
      const name = crypto.randomUUID();

      const response = await send("/api/addItem", {
        body: JSON.stringify({ name }),
        headers: { host, origin: `http://${host}` },
      });

      expect(response.status).toBe(200);
      expect(JSON.parse(response.body)).toMatchObject({
        result: { data: { name } },
      });
    },
  );

  it("accepts a write without an origin from a client that is not a browser", async () => {
    await start();
    const name = crypto.randomUUID();

    const response = await send("/api/addItem", {
      body: JSON.stringify({ name }),
    });

    expect(response.status).toBe(200);
    expect(JSON.parse(response.body)).toMatchObject({
      result: { data: { name } },
    });
  });

  it("refuses an MCP request from another site's page", async () => {
    await start();

    const response = await send("/mcp", {
      body: "{}",
      headers: { origin: anotherSite() },
    });

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
      body: "<h1>Slate</h1>",
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
      "default-src 'self'; style-src 'self' 'unsafe-inline'; " +
        "frame-ancestors 'none'; base-uri 'none'; form-action 'none'",
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
