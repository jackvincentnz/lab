interface CsrfToken {
  headerName: string;
  token: string;
}

/** Share a session token across GraphQL requests, including concurrent page-load queries. */
export function createCsrfFetch(): typeof fetch {
  let csrf: Promise<CsrfToken | undefined> | undefined;

  async function loadToken(): Promise<CsrfToken | undefined> {
    const response = await fetch("/api/csrf", {
      credentials: "same-origin",
      cache: "no-store",
      headers: { Accept: "application/json" },
    });
    // Direct Mops development has no gateway or CSRF endpoint.
    if (response.status === 404) return undefined;
    if (!response.ok) {
      throw new Error(`Unable to obtain CSRF token (${response.status})`);
    }
    return response.json() as Promise<CsrfToken>;
  }

  return async (input, init) => {
    csrf ??= loadToken().catch((error: unknown) => {
      csrf = undefined;
      throw error;
    });
    const token = await csrf;
    const headers = new Headers(init?.headers);
    if (token) headers.set(token.headerName, token.token);
    const response = await fetch(input, { ...init, headers });
    // A later request can obtain fresh state; never automatically replay a mutation.
    if (response.status === 401 || response.status === 403) csrf = undefined;
    return response;
  };
}

export const csrfFetch = createCsrfFetch();
