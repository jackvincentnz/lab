# Mops

Full-stack budgeting assistant stack: a Spring Boot service (GraphQL + AI), a Vite + React app,
and supporting eval tooling.

## Getting started

Run service + app together:

```zsh
bazel run //projects/mops
```

Run the service only:

```zsh
bazel run //projects/mops/service
```

See the [Mops App guide](app/README.md) for app build, run and watch commands.

### Environment variables

The service expects an AI provider API key. By default it uses Gemini.

- `GEMINI_API_KEY`: required for the default `spring.ai.model.chat=google-genai` configuration.
- `OPENROUTER_API_KEY`: required if you switch to the OpenRouter config in `projects/mops/service/src/main/resources/application.properties`.
- `OPENAI_API_KEY`: required if you switch to the OpenAI config in `projects/mops/service/src/main/resources/application.properties`.

## Identity

Mops sits behind the edge gateway (see the [Edge gateway ADR](../../docs/adr/gateway.md)) and
expects every non-health request to carry the identity contract headers `X-Principal-Id`,
`X-Tenant-Id`, and `X-Scopes`. Requests without a complete, well-formed set are rejected with
`401` before dispatch. The parsed identity is available to application code through
`lab.libs.identity.IdentityHolder`.

The `dev` profile sets `mops.identity.development.enabled=true`, so the direct targets above,
the app dev server, the eval runner, and the e2e image all run without a gateway by assuming a
fixed development identity. Leave it unset anywhere the gateway fronts the service.

## Tests

Run service tests:

```zsh
bazel test //projects/mops/service/src/test/java/lab/mops:tests
```

See the [Mops App test commands](app/README.md#development) for one-off and watch runs.

Run the full-stack Playwright smoke test:

```zsh
bazel test //projects/mops/e2e
```

## Local endpoints

- Service base URL: `http://localhost:8080`
- MCP streamable HTTP endpoint: `/sse`
- GraphQL HTTP + WS: `/graphql`
- App dev server (Vite default): `http://localhost:5173` (proxies `/api` and `/ws` to the service)

## Project map

- `projects/mops/service/src/main/java/lab/mops`: Spring Boot entrypoint (`MopsApplication`) and domain modules.
- `projects/mops/service/src/main/resources/schema/schema.graphqls`: GraphQL schema.
- `projects/mops/service/bruno_collection`: Bruno API request collection.
- `projects/mops/app/src`: React app sources.
- `projects/mops/eval`: evaluation runner and question sets (hits the service at `localhost:8080`).

## Related docs

- `projects/mops/app/README.md`
