# Gateway CSRF

## Status

Proposed.

Date: 2026-09-27
Decision owner: Jack Vincent

## Context

The [edge gateway](gateway.md) authenticates browsers with a session cookie. Browsers attach
cookies automatically, so an authenticated session alone does not protect unsafe requests from
CSRF. Mops is a separately served SPA, and the gateway already requires Redis sessions.

## Decision

Keep Spring Security's session-backed CSRF validation and default token masking. Authenticated
clients obtain a token from `GET /api/csrf` and send it in `X-CSRF-TOKEN` on unsafe requests.
The token response is not cacheable; the SPA holds it in memory and shares acquisition between
concurrent queries. Authentication or CSRF rejection clears that cached token for the next
request; mutations are never replayed automatically.

This preserves Spring's defaults and binds the expected token to the session. It costs one extra
request per page load, plus reacquisition after rejection. The session cookie remains `HttpOnly`;
CSRF validation happens at the gateway, which strips cookies before forwarding downstream.

## Alternatives considered

- **Deliver the session-backed token in a readable cookie.** Avoids the token-fetch request if
  issued with the page, but adds cookie scope, issuance, refresh, and masking-handling concerns.
  This changes token delivery, not the session-backed validation model.
- **Signed, session-bound double-submit cookie.** Can avoid server-side CSRF token storage, but
  adds signing and cookie-handling concerns while our browser sessions still require Redis.
  Naive cookie/header equality is susceptible to cookie injection.
- **Embed the token in HTML.** Avoids a separate request, but requires gateway rendering or
  rewriting of downstream HTML and care around caching.

## Reconsider when

- Measured token-fetch latency materially delays page load, or multiple SPAs repeatedly need
  acquisition logic: evaluate readable-cookie delivery while retaining session-backed validation.
- The gateway starts rendering the application shell: evaluate embedding the token in HTML.
- Browser authentication no longer requires server-side sessions: reevaluate signed double-submit
  alongside the new authentication model.
- An API uses only explicitly attached credentials, with no cookie authentication: assess whether
  that API needs CSRF protection; retain it for browser session endpoints.

## References

- [Spring Security reactive CSRF](https://docs.spring.io/spring-security/reference/reactive/exploits/csrf.html).
- [OWASP CSRF prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html).
