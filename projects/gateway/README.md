# Gateway

Edge gateway for the lab verticals, built on Spring Boot and Spring Cloud Gateway. It is the
only public entry point and will own authentication, sessions, and the identity contract handed
to verticals. See the [Edge gateway ADR](../../docs/adr/gateway.md) for the design and the
first-iteration scope.

The service routes one public host to verticals by path prefix and exposes a public health
endpoint. Login, sessions, and the identity contract land in follow-up issues tracked in
[#962](https://github.com/jackvincentnz/lab/issues/962).

## Getting started

Run the service:

```zsh
bazel run //projects/gateway
```

Run Mops through the gateway by starting the Mops service and app alongside it, then open
`http://localhost:3006/mops/`:

```zsh
bazel run //projects/mops
bazel run //projects/gateway
```

Downstream targets default to local dev and are overridable per environment, for example
`LAB_GATEWAY_MOPS_SERVICE_URI=http://mops:8080`.

## Routes

| Public path        | Downstream                                   | Notes                                       |
| ------------------ | -------------------------------------------- | ------------------------------------------- |
| `/mops/api/**`     | Mops service, `lab.gateway.mops.service-uri` | `/mops/api` prefix is stripped.             |
| `/mops/**`         | Mops app, `lab.gateway.mops.app-uri`         | Passed through; the app's base is `/mops/`. |
| `/actuator/health` | Gateway                                      | Public.                                     |

GraphQL subscriptions over WebSocket are not proxied yet
([#953](https://github.com/jackvincentnz/lab/issues/953)).

## Tests

Run service tests:

```zsh
bazel test //projects/gateway/...
```

## Local endpoints

- Service base URL: `http://localhost:3006`
- Mops through the gateway: `http://localhost:3006/mops/`
- Health: `/actuator/health`

## Project map

- `projects/gateway/src/main/java/lab/gateway`: Spring Boot entrypoint (`GatewayApplication`).
- `projects/gateway/src/main/resources/application.yaml`: port, routes, downstream URIs, and actuator exposure.
