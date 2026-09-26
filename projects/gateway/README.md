# Gateway

Edge gateway for the lab verticals, built on Spring Boot and Spring Cloud Gateway. It is the
only public entry point and owns authentication, sessions, and the identity contract handed to
verticals. See the [Edge gateway ADR](../../docs/adr/gateway.md) for the design.

The service routes one public host to Mops and exposes a public health endpoint.

## Getting started

Run the service:

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
