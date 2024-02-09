import * as net from "net";

export function findUnusedPort(startingPort: number): Promise<number> {
  return new Promise((resolve, reject) => {
    const server = net.createServer();

    server.on("error", (err: NodeJS.ErrnoException) => {
      if (err.code === "EADDRINUSE") {
        findUnusedPort(startingPort + 1).then(resolve, reject);
      } else {
        reject(err);
      }
    });

    server.on("listening", () => {
      server.close(() => {
        resolve(startingPort);
      });
    });

    server.listen(startingPort, "127.0.0.1");
  });
}
