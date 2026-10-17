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

- `Authorization: Bearer <identity token>`, minted for the route's audience. Any `Authorization`
  the client sent is dropped.
- No `Cookie`.
- The gateway's `X-Request-ID`.
- `Host` set to the service's own address.
- The subprotocols the client requested. The gateway accepts the client's first choice itself,
  before it connects to the service, so the service must accept that subprotocol too.

Because the gateway accepts the client's handshake before it connects to the service, a service
that refuses the handshake shows up to the client as a socket that opens and then closes, not as a
failed handshake.

The access log writes one record per upgrade when the gateway accepts the handshake. Its `status`
is 200, although the client receives 101, because the record is written as WebFlux commits the
response and the server sends the 101 after that. Nothing is logged when the socket closes.

## Open items

### Origin and Host

The gateway sends the service its own address as `Host`. A WebSocket handler that compares the
browser's `Origin` with the request's scheme, host, and port therefore rejects every proxied
browser handshake. Spring GraphQL adds that check only when `spring.graphql.cors.allowed-origins`
or `allowed-origin-patterns` is set, and accepts listed origins regardless of `Host`. Options if a
service needs it:

- Configure the public origin as an allowed origin in the service. The handshake check is explicit
  and nothing else changes, at the cost of one setting per environment.
- Check `Origin` at the gateway against a configured public origin, for upgrades only. Cross-site
  WebSocket protection then lives in one place for every service.
- Add `PreserveHostHeader` to the API route. The service sees the public `Host` on every API
  request, which changes any absolute URL it builds and breaks a host-routed proxy between the
  gateway and the service. It is not enough behind TLS termination: the service still sees `http`,
  and the same-origin check compares the scheme.
- Forward `X-Forwarded-Host` and `X-Forwarded-Proto`. The gateway strips these unless
  `spring.cloud.gateway.server.webflux.trusted-proxies` is set, and the pattern must match the
  gateway's own peer, so exposed directly to browsers it trusts whatever clients send. The service
  must also honour them, which changes its view of scheme, host, and client address for every
  request and requires it to be reachable only through the gateway.

The session cookie is `SameSite=Lax`, so a cross-site page cannot open an authenticated socket.
A same-site page on another origin can.

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
