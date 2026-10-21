import { afterEach, describe, expect, it, vi } from "vitest";
import { createCsrfFetch } from "./csrfFetch";

const tokenResponse = (token: string) =>
  Response.json({ headerName: "X-CSRF-TOKEN", token });
const statusResponse = (status: number) => new Response(null, { status });
const okResponse = () => new Response("ok");

/** Replaces the browser's fetch; each response answers the next call, in order. */
function stubFetch(...responses: Response[]) {
  const fetchMock = vi.fn();
  for (const response of responses) fetchMock.mockResolvedValueOnce(response);
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

const requestsTo = (fetchMock: ReturnType<typeof vi.fn>, url: string) =>
  fetchMock.mock.calls.filter(([calledUrl]) => calledUrl === url);

const sentHeader = (
  fetchMock: ReturnType<typeof vi.fn>,
  call: number,
  name: string,
) =>
  (
    fetchMock.mock.calls[call][1] as RequestInit & { headers: Headers }
  ).headers.get(name);

afterEach(() => vi.unstubAllGlobals());

describe("csrfFetch", () => {
  it("sends the gateway token with the original GraphQL request", async () => {
    const graphqlResponse = okResponse();
    const fetchMock = stubFetch(
      tokenResponse("session-token"),
      graphqlResponse,
    );
    const init = {
      method: "POST",
      body: '{"query":"query { allLineItems { id } }"}',
      headers: { "Content-Type": "application/json" },
    };

    const response = await createCsrfFetch()("/api/graphql", init);

    expect(response).toBe(graphqlResponse);
    expect(fetchMock.mock.calls[0]).toEqual([
      "/api/csrf",
      expect.objectContaining({
        credentials: "same-origin",
        cache: "no-store",
      }),
    ]);
    expect(fetchMock.mock.calls[1][0]).toBe("/api/graphql");
    expect(fetchMock.mock.calls[1][1]).toMatchObject({
      method: "POST",
      body: init.body,
    });
    expect(sentHeader(fetchMock, 1, "Content-Type")).toBe("application/json");
    expect(sentHeader(fetchMock, 1, "X-CSRF-TOKEN")).toBe("session-token");
  });

  it("supports direct Mops development without a gateway", async () => {
    const fetchMock = stubFetch(statusResponse(404), okResponse());
    const init = { method: "POST", body: "query" };

    await createCsrfFetch()("/api/graphql", init);

    expect(fetchMock).toHaveBeenLastCalledWith(
      "/api/graphql",
      expect.objectContaining(init),
    );
  });

  it("shares token retrieval between concurrent queries", async () => {
    const fetchMock = stubFetch(
      tokenResponse("shared-token"),
      okResponse(),
      okResponse(),
    );
    const csrfFetch = createCsrfFetch();

    await Promise.all([
      csrfFetch("/api/graphql", { method: "POST" }),
      csrfFetch("/api/graphql", { method: "POST" }),
    ]);

    expect(requestsTo(fetchMock, "/api/csrf")).toHaveLength(1);
    expect(requestsTo(fetchMock, "/api/graphql")).toHaveLength(2);
  });

  it("returns to login when the session has expired", async () => {
    const fetchMock = stubFetch(statusResponse(401));
    const login = vi.fn();

    const request = createCsrfFetch(login)("/api/graphql", { method: "POST" });

    await expect(request).rejects.toThrow("Unable to obtain CSRF token (401)");
    expect(login).toHaveBeenCalledOnce();
    expect(requestsTo(fetchMock, "/api/graphql")).toHaveLength(0);
  });

  it("returns to login when a request is unauthorized", async () => {
    stubFetch(tokenResponse("stale"), statusResponse(401));
    const login = vi.fn();

    const response = await createCsrfFetch(login)("/api/graphql", {
      method: "POST",
    });

    expect(response.status).toBe(401);
    expect(login).toHaveBeenCalledOnce();
  });

  it("refreshes the token after a rejected request without replaying it", async () => {
    const fetchMock = stubFetch(
      tokenResponse("old"),
      statusResponse(403),
      tokenResponse("new"),
      okResponse(),
    );
    const csrfFetch = createCsrfFetch();

    const rejected = await csrfFetch("/api/graphql", { method: "POST" });
    const retried = await csrfFetch("/api/graphql", { method: "POST" });

    expect(rejected.status).toBe(403);
    expect(retried.ok).toBe(true);
    expect(requestsTo(fetchMock, "/api/graphql")).toHaveLength(2);
    expect(sentHeader(fetchMock, 3, "X-CSRF-TOKEN")).toBe("new");
  });

  it("retries token retrieval after a temporary failure", async () => {
    stubFetch(statusResponse(503), tokenResponse("fresh"), okResponse());
    const csrfFetch = createCsrfFetch();

    await expect(csrfFetch("/api/graphql")).rejects.toThrow("503");
    const response = await csrfFetch("/api/graphql");

    expect(response.ok).toBe(true);
  });
});
