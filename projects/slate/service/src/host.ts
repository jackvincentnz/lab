const loopbackHostnames = new Set(["localhost", "127.0.0.1", "[::1]"]);

// A web page that points its own domain at 127.0.0.1 still sends that domain in `Host`, so checking
// the name stops it reaching the service.
export function isLoopbackHost(host: string | undefined): boolean {
  const hostname = URL.parse(`http://${host}`)?.hostname;
  return hostname !== undefined && loopbackHostnames.has(hostname);
}

export function isServiceOrigin(
  origin: string | undefined,
  host: string | undefined,
): boolean {
  return origin === undefined || origin === `http://${host}`;
}
