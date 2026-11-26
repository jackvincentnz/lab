---
uses: [libs/test]
---

# WIDE

Work Intake & Delivery Engine: a local, single-user experiment in agent-assisted product and
engineering refinement. A Spring Boot service stores signals, strategy, problems, candidate
solutions, questions, and decisions in Postgres and exposes them over MCP and a read-only GraphQL
API. A connected agent does the reasoning through the WIDE skills, and a Vite + React explorer
browses the saved work. See the [design](docs/design.md) for the concepts.

## Getting started

Follow the [monorepo setup](../../README.md#getting-started) and start Docker. Run these commands
from the repository root.

Run service + app together:

```zsh
bazel run //projects/wide
```

Run the service only:

```zsh
bazel run //projects/wide/service
```

The service starts Postgres from `docker-compose.yml` through Spring Boot's Docker Compose support
and points its datasource at it. On exit it stops the container, never removes it, so the next run
reuses it and its volume.

See the [WIDE App guide](app/README.md) for app build, run and watch commands.

### Environment variables

The run targets activate the `local` Spring profile and connect to the Postgres they start, so no
variables are needed locally. The database variables apply when the service does not start Postgres:
pass `--spring.docker.compose.enabled=false` after `--` to use them under the `local` profile, whose
defaults match the compose file. Without that profile they are required.

- `WIDE_PORT`: service port, default `8095`. Set it for the app too, which proxies to the service.
- `WIDE_DATABASE_URL`: JDBC URL, `local` default `jdbc:postgresql://127.0.0.1:5495/wide`.
- `WIDE_DATABASE_USER`: database user, `local` default `wide`.
- `WIDE_DATABASE_PASSWORD`: database password, `local` default `wide-local`.

### Data

Records persist in the Docker volume `wide_wide-db` across service restarts and
`docker compose down`; `docker compose down -v` deletes them. Flyway applies migrations when the
service starts.

## Connect an agent

Keep the service running, then register its MCP endpoint and install the
[intake](skills/wide-intake/SKILL.md), [triage](skills/wide-triage/SKILL.md), and
[refine](skills/wide-refine/SKILL.md) skills in your client. Run the commands below from the
repository root; the skill symlinks follow changes in the repository.

### Codex

```zsh
codex mcp add wide --url http://localhost:8095/mcp
mkdir -p ~/.agents/skills
for skill in wide-intake wide-triage wide-refine; do
  ln -s "$PWD/projects/wide/skills/$skill" "$HOME/.agents/skills/$skill"
done
```

### Claude Code

```zsh
claude mcp add --transport http --scope user wide http://localhost:8095/mcp
mkdir -p ~/.claude/skills
for skill in wide-intake wide-triage wide-refine; do
  ln -s "$PWD/projects/wide/skills/$skill" "$HOME/.claude/skills/$skill"
done
```

### Claude Desktop

Bridge to the HTTP endpoint with [mcp-remote](https://github.com/punkpeye/mcp-remote), which needs
Node. Merge this entry into `~/Library/Application Support/Claude/claude_desktop_config.json`, then
restart the client:

```json
{
  "mcpServers": {
    "wide": {
      "command": "npx",
      "args": ["-y", "mcp-remote@0.16.7", "http://localhost:8095/mcp", "--allow-http"]
    }
  }
}
```

Add the three skill folders, including `wide-refine/references`, through the client's skill
settings.

With synthetic content, ask the agent to capture a signal, set a strategy, triage the collection,
and refine its recommendation. Refresh the explorer to see what it saved.

## Tests

Run service tests:

```zsh
bazel test //projects/wide/service/...
```

Unit tests need nothing else. The `domain` repository tests against Postgres and the `functional` tests
over MCP and GraphQL use throwaway Testcontainers databases, never the local one, and are tagged
`requires-docker`.

See the [WIDE App test commands](app/README.md#development) for one-off and watch runs.

## Local endpoints

- Explorer: `http://localhost:5195` (proxies `/api/graphql` to the service)
- MCP Streamable HTTP endpoint: `http://localhost:8095/mcp`
- GraphQL HTTP (read-only): `http://localhost:8095/graphql`
- Health: `http://localhost:8095/actuator/health`
- Postgres: `127.0.0.1:5495`, database and user `wide`, password `wide-local`

The `local` profile binds the service to loopback, and the compose file publishes Postgres on
loopback only. There is no authentication and no model API key.

## Project map

- `projects/wide/service/src/main/java/lab/wide`: Spring Boot entrypoint (`WideApplication`) and the
  `api`, `application`, `config`, and `domain` packages.
- `projects/wide/service/src/main/resources/schema/schema.graphqls`: GraphQL schema.
- `projects/wide/service/src/main/resources/db/migration`: Flyway migrations.
- `projects/wide/app/src`: React explorer sources.
- `projects/wide/skills`: agent skills for intake, triage, and refinement, with the refinement
  guides the explorer also renders.

## Related docs

- [WIDE App](app/README.md).
- [Design](docs/design.md).
- [Contributor rules](AGENTS.md).
