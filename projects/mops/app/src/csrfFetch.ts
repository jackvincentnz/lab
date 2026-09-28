interface CsrfToken {
  headerName: string;
  token: string;
}

/**
 * A fetch that attaches the gateway's session CSRF token to every request.
 *
 * `login` is injectable so tests can observe a redirect without navigating.
 */
export function createCsrfFetch(
  login: () => void = () => window.location.assign("/login"),
): typeof fetch {
  // The token lookup, not the token. Requests that start while it is in flight
  // await the same promise, so a page load fetches the token once. Once settled,
  // awaiting it returns the token immediately, which makes this the cache too.
  let csrf: Promise<CsrfToken | undefined> | undefined;

  async function loadToken(): Promise<CsrfToken | undefined> {
    const response = await fetch("/api/csrf", {
      credentials: "same-origin",
      cache: "no-store",
      headers: { Accept: "application/json" },
    });
    // Direct Mops development has no gateway or CSRF endpoint.
    if (response.status === 404) return undefined;
    if (response.status === 401) login();
    if (!response.ok) {
      throw new Error(`Unable to obtain CSRF token (${response.status})`);
    }
    return response.json() as Promise<CsrfToken>;
  }

  return async (input, init) => {
    // Only the first request starts the lookup. A failed lookup clears the cache
    // so the next request retries instead of awaiting a rejected promise.
    csrf ??= loadToken().catch((error: unknown) => {
      csrf = undefined;
      throw error;
    });
    const token = await csrf;
    const headers = new Headers(init?.headers);
    if (token) headers.set(token.headerName, token.token);
    const response = await fetch(input, { ...init, headers });
    // A rejected token is stale, so forget it and let the next request fetch a
    // fresh one. Never replay the failed request: it may have been a mutation.
    if (response.status === 401 || response.status === 403) csrf = undefined;
    if (response.status === 401) login();
    return response;
  };
}

export const csrfFetch = createCsrfFetch();
