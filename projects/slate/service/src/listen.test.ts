import { createServer, type Server } from "node:http";
import { type AddressInfo } from "node:net";
import { afterEach, describe, expect, it } from "vitest";
import { listen } from "./listen.js";

const host = "127.0.0.1";
const servers: Server[] = [];

function server(): Server {
  const server = createServer();
  servers.push(server);
  return server;
}

afterEach(async () => {
  await Promise.all(
    servers
      .splice(0)
      .filter((server) => server.listening)
      .map((server) => new Promise((resolve) => server.close(resolve))),
  );
});

describe("listen", () => {
  it("resolves with the URL the server listens on", async () => {
    const listening = server();

    const url = await listen(listening, 0, host);

    const { port } = listening.address() as AddressInfo;
    expect(url).toBe(`http://${host}:${port}`);
  });

  it("names the port when another program already uses it", async () => {
    const taken = server();
    await listen(taken, 0, host);
    const { port } = taken.address() as AddressInfo;

    const listening = listen(server(), port, host);

    await expect(listening).rejects.toThrow(
      `Port ${port} on ${host} is already in use`,
    );
  });
});
