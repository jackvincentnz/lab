import { mkdtempSync, rmSync } from "node:fs";
import { request, type Server } from "node:http";
import { type AddressInfo } from "node:net";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createDayServer } from "./server.js";
import { openItemStore } from "./store.js";

let dataDir: string;
let server: Server;

beforeEach(async () => {
  dataDir = mkdtempSync(join(tmpdir(), "day-server-"));
  server = createDayServer(openItemStore(dataDir));
  await new Promise<void>((resolve) => server.listen(0, "127.0.0.1", resolve));
});

afterEach(async () => {
  await new Promise((resolve) => server.close(resolve));
  rmSync(dataDir, { recursive: true, force: true });
});

interface Response {
  status: number;
  body: string;
}

function send(path: string, host: string, body?: string): Promise<Response> {
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
          resolve({ status: response.statusCode ?? 0, body }),
        );
      },
    )
      .on("error", reject)
      .end(body);
  });
}

describe("createDayServer", () => {
  it("serves the router to a loopback host", async () => {
    await send("/addItem", "localhost:5197", '{"name":"Plan the week"}');

    const response = await send("/listItems", "localhost:5197");

    expect(response.status).toBe(200);
    expect(JSON.parse(response.body)).toMatchObject({
      result: { data: [{ name: "Plan the week", status: "todo" }] },
    });
  });

  it("refuses a request for another host", async () => {
    const response = await send("/listItems", "attacker.example:5197");

    expect(response.status).toBe(403);
  });
});
