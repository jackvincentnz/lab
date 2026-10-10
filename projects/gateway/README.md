# Gateway

Edge gateway for the lab services, built on Spring Boot and Spring Cloud Gateway. It is the
only public entry point and owns authentication, sessions, and the identity contract handed to
downstream services. See the [Edge gateway ADR](../../docs/adr/gateway.md) for the design.

The service routes one public host to Mops, forwards each API request with a signed identity
token, and exposes public health and JWK set endpoints.

## Getting started

Start the gateway, Redis, and the Mops service and app, then open `http://localhost:3006` and
log in as `admin` with password `admin`:

```zsh
bazel run //projects/gateway:start
```

Run the gateway and Redis on their own:

```zsh
bazel run //projects/gateway
```

Both targets need a running Docker engine, activate the `local` profile, which supplies the
`admin` user, and start Redis from `compose.yaml`. Ctrl-C stops everything they started. See
[Local stack](docs/local-stack.md) for what runs and how it is wired, and the
[Mops README](../mops/README.md#environment-variables) for the API key the Mops service expects.

Redis publishes on host port 6379 unless `GATEWAY_REDIS_PORT` sets another, so stacks in
several worktrees can run at once.

Downstream targets default to local dev and are overridable per environment, for example
`LAB_GATEWAY_MOPS_SERVICE_URI=http://mops:8080`.

## Routes

| Public path              | Downstream                                   | Notes                                            |
| ------------------------ | -------------------------------------------- | ------------------------------------------------ |
| `/actuator/health`       | Gateway                                      | Public.                                          |
| `/.well-known/jwks.json` | Gateway                                      | Public. Keys that verify identity tokens.        |
| `/api/csrf`              | Gateway                                      | Session CSRF token.                              |
| `/api/**`                | Mops service, `lab.gateway.mops.service-uri` | `/api` prefix is stripped. Identity token added. |
| `/**`                    | Mops app, `lab.gateway.mops.app-uri`         | Passed through unchanged.                        |

Every response carries a gateway-minted `X-Request-ID`, which is also forwarded downstream. See
[Access logging](docs/access-logging.md) for the record written per request.

GraphQL subscriptions over WebSocket are not proxied. Session-based API callers send the token
from `/api/csrf` as `X-CSRF-TOKEN` on unsafe requests; the Mops app does this itself.

## Signing key

The `local` profile generates the token signing key at startup. Anywhere else, supply a PKCS#8
PEM RSA private key through the environment, see [Signing key](docs/signing-key.md):

```zsh
LAB_GATEWAY_TOKEN_PRIVATE_KEY="$(cat gateway-signing-key.pem)" bazel run //projects/gateway
```

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

## Users

Without the `local` profile no users exist. Configure users in a file outside version control and
load it with `SPRING_CONFIG_ADDITIONAL_LOCATION=file:/absolute/path/gateway-local.yaml`:

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
