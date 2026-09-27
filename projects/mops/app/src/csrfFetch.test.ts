import { afterEach, describe, expect, it, vi } from "vitest";
import { createCsrfFetch } from "./csrfFetch";

afterEach(() => vi.unstubAllGlobals());

describe("csrfFetch", () => {
  it("sends the gateway token with the original GraphQL request", async () => {
    const graphqlResponse = new Response('{"data":{}}');
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        Response.json({ headerName: "X-CSRF-TOKEN", token: "session-token" }),
      )
      .mockResolvedValueOnce(graphqlResponse);
    vi.stubGlobal("fetch", fetchMock);
    const init = {
      method: "POST",
      body: '{"query":"query { allLineItems { id } }"}',
      headers: { "Content-Type": "application/json" },
    };

    expect(await createCsrfFetch()("/api/graphql", init)).toBe(graphqlResponse);
    expect(fetchMock.mock.calls[0]).toEqual([
      "/api/csrf",
      expect.objectContaining({
        credentials: "same-origin",
        cache: "no-store",
      }),
    ]);
    const [url, request] = fetchMock.mock.calls[1];
    expect(url).toBe("/api/graphql");
    expect(request.method).toBe("POST");
    expect(request.body).toBe(init.body);
    expect(request.headers.get("Content-Type")).toBe("application/json");
    expect(request.headers.get("X-CSRF-TOKEN")).toBe("session-token");
  });

  it("supports direct Mops development without a gateway", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(new Response(null, { status: 404 }))
      .mockResolvedValueOnce(new Response("ok"));
    vi.stubGlobal("fetch", fetchMock);
    const init = { method: "POST", body: "query" };
    await createCsrfFetch()("/api/graphql", init);
    expect(fetchMock).toHaveBeenLastCalledWith(
      "/api/graphql",
      expect.objectContaining(init),
    );
  });

  it("shares token retrieval between concurrent queries", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        Response.json({ headerName: "X-CSRF-TOKEN", token: "shared-token" }),
      )
      .mockResolvedValue(new Response("ok"));
    vi.stubGlobal("fetch", fetchMock);
    const csrfFetch = createCsrfFetch();
    await Promise.all([
      csrfFetch("/api/graphql", { method: "POST" }),
      csrfFetch("/api/graphql", { method: "POST" }),
    ]);
    expect(
      fetchMock.mock.calls.filter(([url]) => url === "/api/csrf"),
    ).toHaveLength(1);
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });

  it("returns to login when the session has expired", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValue(new Response(null, { status: 401 }));
    vi.stubGlobal("fetch", fetchMock);
    const login = vi.fn();
    await expect(
      createCsrfFetch(login)("/api/graphql", { method: "POST" }),
    ).rejects.toThrow("Unable to obtain CSRF token (401)");
    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(login).toHaveBeenCalledOnce();
  });

  it("returns to login when a request is unauthorized", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        Response.json({ headerName: "X-CSRF-TOKEN", token: "stale" }),
      )
      .mockResolvedValueOnce(new Response(null, { status: 401 }));
    vi.stubGlobal("fetch", fetchMock);
    const login = vi.fn();
    const response = await createCsrfFetch(login)("/api/graphql", {
      method: "POST",
    });
    expect(response.status).toBe(401);
    expect(login).toHaveBeenCalledOnce();
  });

  it("refreshes after a rejected request without replaying it", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        Response.json({ headerName: "X-CSRF-TOKEN", token: "old" }),
      )
      .mockResolvedValueOnce(new Response(null, { status: 403 }))
      .mockResolvedValueOnce(
        Response.json({ headerName: "X-CSRF-TOKEN", token: "new" }),
      )
      .mockResolvedValueOnce(new Response("ok"));
    vi.stubGlobal("fetch", fetchMock);
    const csrfFetch = createCsrfFetch();
    expect((await csrfFetch("/api/graphql", { method: "POST" })).status).toBe(
      403,
    );
    expect(fetchMock).toHaveBeenCalledTimes(2);
    await csrfFetch("/api/graphql", { method: "POST" });
    expect(fetchMock.mock.calls[3][1].headers.get("X-CSRF-TOKEN")).toBe("new");
  });

  it("can retry token retrieval after a temporary failure", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(new Response(null, { status: 503 }))
      .mockResolvedValueOnce(
        Response.json({ headerName: "X-CSRF-TOKEN", token: "fresh" }),
      )
      .mockResolvedValueOnce(new Response("ok"));
    vi.stubGlobal("fetch", fetchMock);
    const csrfFetch = createCsrfFetch();
    await expect(csrfFetch("/api/graphql")).rejects.toThrow("503");
    expect((await csrfFetch("/api/graphql")).ok).toBe(true);
  });
});
