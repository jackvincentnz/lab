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

Run the app only:

```zsh
bazel run //projects/mops/app
```

Run the app in watch mode (ibazel):

```zsh
ibazel run //projects/mops/app
```

### Environment variables

The service expects an AI provider API key. By default it uses Gemini.

- `GEMINI_API_KEY`: required for the default `spring.ai.model.chat=google-genai` configuration.
- `OPENROUTER_API_KEY`: required if you switch to the OpenRouter config in `projects/mops/service/src/main/resources/application.properties`.
- `OPENAI_API_KEY`: required if you switch to the OpenAI config in `projects/mops/service/src/main/resources/application.properties`.
- `GOOGLE_API_KEY`: required for the ADK agent UI in `projects/mops/adk_agent`.

## Tests

Run service tests:

```zsh
bazel test //projects/mops/service/src/test/java/lab/mops:tests
```

Run app tests (one-off):

```zsh
bazel test //projects/mops/app:test_run
```

Run app tests in watch mode:

```zsh
bazel run //projects/mops/app:test
```

Run app tests in watch mode with ibazel:

```zsh
ibazel run //projects/mops/app:test
```

## Local endpoints

- Service base URL: `http://localhost:8080`
- AI REST endpoints: `POST /chats`, `POST /chats/{chatId}`
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
- `projects/mops/adk_agent/README.md`
