const devServerHost = "127.0.0.1";

export interface DevServer {
  port: string;
  url: string;
}

export function readDevServer(env: NodeJS.ProcessEnv): DevServer | undefined {
  if (env["DAY_WEB_DEV_SERVER"] !== "1") {
    return undefined;
  }
  const port = env["DAY_APP_PORT"] || "5196";
  // A port like `80@example.com` would make the URL name another host.
  if (!/^\d+$/.test(port) || Number(port) < 1 || Number(port) > 65535) {
    throw new Error(`DAY_APP_PORT must be a port number, not ${port}`);
  }
  return { port, url: `http://${devServerHost}:${port}` };
}

const timeoutMs = 30_000;
const retryMs = 100;
const requestTimeoutMs = 1_000;

export async function waitForServer(url: string): Promise<void> {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    try {
      await fetch(url, { signal: AbortSignal.timeout(requestTimeoutMs) });
      return;
    } catch {
      await new Promise((resolve) => setTimeout(resolve, retryMs));
    }
  }
  throw new Error(
    `The web UI's dev server did not answer at ${url} within ${timeoutMs} ms`,
  );
}
