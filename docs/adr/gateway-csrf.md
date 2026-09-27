# Gateway CSRF

## Status

Proposed.

Date: 2026-09-27
Decision owner: Jack Vincent

## Context

The [edge gateway](gateway.md) authenticates browsers with a session cookie. Browsers attach
cookies automatically, so a session alone does not protect unsafe requests. Mops is a separately
served SPA, and sessions already require Redis.

## Decision

Keep Spring Security's session-backed CSRF validation and default token masking. Clients fetch
the token from `GET /api/csrf` and send it as `X-CSRF-TOKEN` on unsafe requests. The response is
not cacheable. The SPA holds the token in memory, shares one fetch between concurrent queries, and
drops it after a `401` or `403`. Mutations are never replayed automatically.

This keeps Spring's defaults and binds the token to the session, at the cost of one extra request
per page load. The session cookie stays `HttpOnly`, and the gateway strips cookies before
forwarding downstream.

## Alternatives considered

- **Readable token cookie.** Saves the fetch when issued with the page, but adds cookie scope,
  refresh, and masking concerns. Changes delivery, not validation.
- **Signed double-submit cookie.** Avoids server-side token storage, which sessions need anyway,
  and adds signing. Plain cookie/header equality is open to cookie injection.
- **Token embedded in HTML.** Saves the fetch, but the gateway would have to render or rewrite
  downstream HTML and manage its caching.

## Reconsider when

- Token-fetch latency measurably delays page load, or several SPAs need the same acquisition
  logic: evaluate a readable cookie.
- The gateway renders the application shell: evaluate embedding the token.
- Browser authentication stops needing server-side sessions: evaluate a signed double-submit
  cookie.
- An API accepts only explicitly attached credentials: assess whether it needs CSRF protection.

## References

- [Spring Security reactive CSRF](https://docs.spring.io/spring-security/reference/reactive/exploits/csrf.html).
- [OWASP CSRF prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html).
