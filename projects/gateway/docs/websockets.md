# WebSockets

A WebSocket upgrade is a `GET` request, so it matches the same routes and passes the same session
check as any other request. Spring Cloud Gateway then switches the route's `http` URI to `ws`,
accepts the client's handshake, and opens its own socket to the downstream service. Messages are
relayed both ways, and a close on either side closes the other.

## What crosses the gateway

| Upgrade on | Without a session | With a session                                                                                      |
| ---------- | ----------------- | --------------------------------------------------------------------------------------------------- |
| `/api/**`  | 401               | Reaches the Mops service with the `/api` prefix stripped and an identity token, like an API request |
| `/**`      | 401               | Reaches the Mops app with no identity token                                                         |

A socket that needs the caller's identity must open under `/api`, so a GraphQL subscription
client connects to `/api/graphql`. A handshake carries no `Accept: text/html`, so a browser gets
the 401 rather than the login redirect.

The handshake the service receives is built like a proxied API request:

- `Authorization: Bearer <identity token>`, minted for the route's audience. A bearer the client
  sent is verified as on an API request; any other `Authorization` is dropped.
- No `Cookie`.
- The gateway's `X-Request-ID`.
- `Host` set to the service's own address.
- The subprotocols the client requested. The gateway accepts the client's first choice itself,
  before it connects to the service, so the service must accept that subprotocol too.

Because the gateway accepts the client's handshake before it connects to the service, a service
that refuses the handshake shows up to the client as a socket that opens and then closes, not as a
failed handshake.

The access log writes one record per upgrade, once the gateway has accepted the handshake, while
the socket is still open. Its `status` is 200, although the client receives 101: Reactor Netty
writes the 101 itself during the upgrade and leaves the WebFlux response, which the record reads,
at its default status. `duration_ms` covers the handshake only, and nothing is logged when the
socket closes.

## Origin check in Mops

Spring GraphQL checks the `Origin` of every WebSocket handshake on the Mops GraphQL endpoint. With
no `spring.graphql.cors.allowed-origins` or `allowed-origin-patterns` set, it allows same-origin
handshakes only: the `Origin` must match the scheme, host, and port of the request Mops receives. A
handshake without `Origin`, such as one from a non-browser client, passes.

The gateway sends Mops the service's own address as `Host`, so a proxied browser handshake does not
match its `Origin`, and Mops answers 403. The client sees a socket that opens and then closes. A
browser subscription through the gateway needs one of these:

- Configure the public origin as an allowed origin in Mops, such as
  `spring.graphql.cors.allowed-origins=https://lab.example.com`. The check stays explicit, nothing
  else changes, and it works behind TLS termination because the listed origin is compared as
  written. It costs one setting per environment.
- Add `PreserveHostHeader` to the API route, so Mops sees the public `Host`. It needs no setting
  per environment, but Mops then sees the public `Host` on every API request, which changes any
  absolute URL it builds and breaks a host-routed proxy between the gateway and Mops. It does not
  work behind TLS termination: Mops still receives `http`, and the same-origin check compares the
  scheme too.

The session cookie is `SameSite=Lax`, so a cross-site page cannot open an authenticated socket.
A same-site page on another origin can, unless Mops refuses its origin.

## Open items

### Socket lifetime

A proxied socket is authenticated once, at the handshake. It outlives the five-minute identity
token, logout, and session expiry, until either side closes it. Options:

- The service closes a socket when the token it was opened with expires. The client reconnects
  through the gateway, which checks the session and mints a fresh token, so a socket outlives its
  session by at most the token lifetime. It costs one timer per socket in the service and a
  reconnect every five minutes, and the close code must be one the client retries; the
  `graphql-ws` client treats 4401 as fatal.
- The gateway checks the session on a timer for each proxied socket and closes both sides when it
  is gone. It costs a session store read per socket per interval. The gateway's WebSocket service
  is not a replaceable bean, so this means wrapping it in a bean post-processor.
- The gateway reacts to session-deleted and session-expired events. The Redis session repository
  the gateway uses publishes none; the indexed repository does, but needs Redis keyspace
  notifications, and every gateway instance has to track which sockets belong to which session.
