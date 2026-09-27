# Gateway

Edge gateway for the lab verticals, built on Spring Boot and Spring Cloud Gateway. It is the
only public entry point and owns authentication, sessions, and the identity contract handed to
verticals. See the [Edge gateway ADR](../../docs/adr/gateway.md) for the design.

The service routes one public host to Mops and exposes a public health endpoint.

## Getting started

Start [Redis](#login-and-sessions), then run the service and log in as `admin` with password `admin`.
The Bazel run target activates the `dev` profile:

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
| `/api/csrf`        | Gateway                                      | Session CSRF token.        |
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

Start Redis before running the gateway:

```zsh
docker compose -f projects/gateway/compose.yaml up -d
```

Stop it and discard session data with `docker compose -f projects/gateway/compose.yaml down -v`.
Redis defaults to `localhost:6379`; override it with Spring's `SPRING_DATA_REDIS_*` settings.

The `dev` profile adds `admin:admin` with `mops:read` and `mops:write`. Without it, no users exist.
Configure users in a file outside version control and load it with
`SPRING_CONFIG_ADDITIONAL_LOCATION=file:/absolute/path/gateway-local.yaml`:

```yaml
lab:
  gateway:
    users:
      - username: alice
        password: "{bcrypt}<bcrypt hash>"
        principal: "11111111-1111-1111-1111-111111111111"
        tenant: "22222222-2222-2222-2222-222222222222"
        scopes: ["mops:read", "mops:write"]
```

Passwords use Spring Security's `{id}encodedPassword` format. Principal and tenant are UUIDs.
Duplicate usernames, missing fields, and passwords without an encoder id fail startup. Changes
apply on the next login.

`/login` serves Spring Security's form and `/logout` a confirmation form. Browser requests without
a session are redirected to `/login`; other requests get `401`. Sessions live in Redis under
`lab:gateway:sessions` with a 30-minute idle timeout (`SPRING_SESSION_TIMEOUT`). The browser holds
only an opaque `SESSION` cookie (`HttpOnly`, `Secure`, `SameSite=Lax`), so local browsers must
accept Secure cookies on `localhost`. Login rotates the session ID; logout deletes the session.

Unsafe requests, including login, logout, and API calls, need a CSRF token. The forms carry it;
Mops fetches it from `/api/csrf` and sends it as `X-CSRF-TOKEN`, returning to `/login` on `401`.
Other session-based API callers must do the same.

The gateway strips the `Cookie` header before proxying, so downstreams never see the session. Run
Mops with its `dev` profile behind the gateway so it applies its development identity to forwarded
requests.

Tests use in-memory sessions, except `RedisSessionTest`, which needs Docker for a Redis container.
