# Gateway

Edge gateway for the lab verticals, built on Spring Boot and Spring Cloud Gateway. It is the
only public entry point and will own authentication, sessions, and the identity contract handed
to verticals. See the [Edge gateway ADR](../../docs/adr/gateway.md) for the design and the
first-iteration scope.

The service currently boots an empty WebFlux gateway with a health endpoint. Routing, login,
and sessions land in follow-up issues tracked in
[#962](https://github.com/jackvincentnz/lab/issues/962).

## Getting started

Run the service:

```zsh
bazel run //projects/gateway
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
- `projects/gateway/src/main/resources/application.yaml`: port and actuator exposure.
