# Responses

A request is a page load when its `Accept` header names `text/html` explicitly. A `*/*` is ignored,
so a browser `fetch` is an API call. An unknown or expired session cookie counts as no session,
because Spring Session replaces it with a new, empty session.

| Request                                                      | Response                                                                           |
| ------------------------------------------------------------ | ---------------------------------------------------------------------------------- |
| Page load, no session or an expired one                      | 302 to `/login`                                                                    |
| Page load with a live session                                | Proxied to the Mops app, with no identity token                                    |
| API call or `fetch`, no session and no bearer token          | 401, `WWW-Authenticate: Bearer`                                                    |
| API call with a live session                                 | Proxied to the Mops service with a gateway-minted identity token, `amr` `form`     |
| Unsafe method with a session but no CSRF token               | 403                                                                                |
| Unsafe method with no credentials                            | 403, because the CSRF check runs before authentication                             |
| Valid bearer token, no session                               | Proxied with a gateway-minted identity token, `amr` `bearer`; no CSRF token needed |
| Invalid or expired bearer token                              | 401, `WWW-Authenticate: Bearer error="invalid_token"`, also on a page load         |
| Valid bearer token with an unknown or expired session cookie | Proxied as a bearer request                                                        |
| Live session and any bearer token                            | 400, `WWW-Authenticate: Bearer error="invalid_request"`                            |
| Bearer token while the issuer's key set is unreachable       | 500                                                                                |
| WebSocket handshake without credentials                      | 401, because a handshake never asks for HTML                                       |
| Route reached without a caller, a gateway misconfiguration   | 500                                                                                |
| `/actuator/health` and `/.well-known/jwks.json`              | Public                                                                             |

A 401 means the caller's credential is missing or bad, and the caller can fix it. A 5xx means the
gateway cannot decide, so the caller should retry later.
