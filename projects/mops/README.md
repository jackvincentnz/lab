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

Mops trusts identity tokens from one configured issuer and expects every non-health request to
carry one as a bearer token. `mops.identity.issuer` names the issuer, `mops.identity.jwk-set-uri`
is where it publishes its signing keys, and `mops.identity.audience` is the name Mops expects in
the token's audience. Keys are fetched on the first token, so the service starts without the
issuer running. A missing, expired, foreign, or wrongly addressed token is rejected with `401`. The
resolved identity is available to application code through `lab.libs.identity.IdentityHolder`.
The token's claims are described in the [Edge gateway ADR](../../docs/adr/gateway.md).

The `local` profile sets `mops.identity.development.enabled=true`, so a request that carries no
token is handled as a fixed development identity. The direct targets above, the e2e image, and
the eval runner's service all activate that profile through `SPRING_PROFILES_ACTIVE=local`; the
delivered image ships with no profile and rejects unidentified requests. The
[gateway stack](../gateway/README.md#getting-started) keeps the profile but turns the development
identity off, because every request it forwards carries a token.

## Tests

Run service tests:

```zsh
bazel test //projects/mops/service/...
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
