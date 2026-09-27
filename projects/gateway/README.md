# Gateway

Edge gateway for the lab verticals, built on Spring Boot and Spring Cloud Gateway. It is the
only public entry point and owns authentication, sessions, and the identity contract handed to
verticals. See the [Edge gateway ADR](../../docs/adr/gateway.md) for the design.

The service routes one public host to Mops and exposes a public health endpoint.

## Getting started

Configure [Redis and a login user](#login-and-sessions), then run the service:

```zsh
bazel run //projects/gateway
```

Run Mops through the gateway by starting the Mops service and app alongside it, then open
`http://localhost:3006`:

```zsh
bazel run //projects/mops
bazel run //projects/gateway
```

Downstream targets default to local dev and are overridable per environment, for example
`LAB_GATEWAY_MOPS_SERVICE_URI=http://mops:8080`.

## Routes

| Public path        | Downstream                                   | Notes                      |
| ------------------ | -------------------------------------------- | -------------------------- |
| `/actuator/health` | Gateway                                      | Public.                    |
| `/api/**`          | Mops service, `lab.gateway.mops.service-uri` | `/api` prefix is stripped. |
| `/**`              | Mops app, `lab.gateway.mops.app-uri`         | Passed through unchanged.  |

GraphQL subscriptions over WebSocket are not proxied.

## Tests

Run service tests:

```zsh
bazel test //projects/gateway/...
```

## Local endpoints

- Service base URL: `http://localhost:3006`
- Health: `/actuator/health`

## Project map

- `projects/gateway/src/main/java/lab/gateway`: Spring Boot entrypoint (`GatewayApplication`).
- `projects/gateway/src/main/resources/application.yaml`: port, routes, downstream URIs, and actuator exposure.

## Login and sessions

Start Redis before running the gateway (from the repository root):

```zsh
docker compose -f projects/gateway/compose.yaml up -d
```

Stop Redis and remove its local session data when finished:

```zsh
docker compose -f projects/gateway/compose.yaml down -v
```

Redis defaults to `localhost:6379`. Configure other environments with Spring's
`SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, `SPRING_DATA_REDIS_PASSWORD`, and
`SPRING_DATA_REDIS_SSL_ENABLED` settings.

There are no default users. Supply a local configuration file outside version control and load it
with `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/absolute/path/gateway-local.yaml`:

```yaml
lab:
  gateway:
    users:
      - username: alice
        password: "{bcrypt}<bcrypt hash of your chosen password>"
        principal: "11111111-1111-1111-1111-111111111111"
        tenant: "22222222-2222-2222-2222-222222222222"
        scopes: ["mops:read", "mops:write"]
```

Passwords use Spring Security’s `{id}encodedPassword` format, such as `{bcrypt}` followed by a
BCrypt hash. Spring’s delegating password encoder handles the supported formats. Principal and tenant
are UUIDs; each user has one tenant and a fixed scope set. Duplicate usernames and missing identity fields
fail startup. Configuration changes apply on the next login; existing sessions retain their identity.

Open `/login` to use Spring Security's login form. All downstream routes require authentication;
`/actuator/health` stays public. HTTP Basic and bearer authentication are not enabled. Session
identity is stored in Redis under `lab:gateway:sessions`, with Spring’s default 30-minute idle timeout
(overridable with `SPRING_SESSION_TIMEOUT`). The browser receives only an opaque `SESSION` cookie with `HttpOnly`, `Secure`, `SameSite=Lax`, and
`Path=/`. Login rotates the session ID. Use HTTPS in deployed environments; local browsers must
support Secure cookies on `localhost`, or use local HTTPS.

Open `/logout` and submit the confirmation form to log out. Logout requires a CSRF-protected POST,
deletes the Redis session, and expires the cookie. CSRF protection is enabled for all unsafe
requests, including login, logout, and downstream API calls. The generated forms carry the token;
API callers using a session must send the corresponding CSRF token in `X-CSRF-TOKEN`.

The gateway removes the entire `Cookie` header before proxying requests to either Mops downstream.
Browser session credentials remain at the gateway.

The gateway's signed downstream identity token and Mops browser CSRF integration are subsequent
work; this change establishes the browser session and its identity.

Gateway integration tests require Docker and start an isolated Redis container automatically.
They exercise real form submissions, Redis persistence, session rotation, cookie attributes,
CSRF rejection, and logout, alongside authenticated routing.
