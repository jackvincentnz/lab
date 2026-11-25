import { type Server } from "node:http";
import { type AddressInfo } from "node:net";

/** Starts `server` listening and resolves with its URL, or rejects with a reason a user can act on. */
export function listen(
  server: Server,
  port: number,
  host: string,
): Promise<string> {
  return new Promise((resolve, reject) => {
    const fail = (error: NodeJS.ErrnoException) =>
      reject(new Error(listenFailure(error, port, host), { cause: error }));
    server.once("error", fail);
    server.listen(port, host, () => {
      server.off("error", fail);
      const { address, port: boundPort } = server.address() as AddressInfo;
      resolve(`http://${address}:${boundPort}`);
    });
  });
}

function listenFailure(
  error: NodeJS.ErrnoException,
  port: number,
  host: string,
): string {
  if (error.code === "EADDRINUSE") {
    return `Port ${port} on ${host} is already in use, for example by another Slate service. Quit the program that uses it, then start Slate again.`;
  }
  return `The Slate service cannot listen on ${host}:${port}: ${error.message}`;
}
