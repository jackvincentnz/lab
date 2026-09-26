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
- App dev server: `http://localhost:5173/mops/` (proxies `/mops/api` and `/mops/ws` to the service)
- Through the [gateway](../gateway/README.md): `http://localhost:3006/mops/`

## Project map

- `projects/mops/service/src/main/java/lab/mops`: Spring Boot entrypoint (`MopsApplication`) and domain modules.
- `projects/mops/service/src/main/resources/schema/schema.graphqls`: GraphQL schema.
- `projects/mops/service/bruno_collection`: Bruno API request collection.
- `projects/mops/app/src`: React app sources.
- `projects/mops/eval`: evaluation runner and question sets (hits the service at `localhost:8080`).

## Related docs

- `projects/mops/app/README.md`
