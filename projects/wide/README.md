# WIDE

Work Intake & Delivery Engine: a local experiment in agent-assisted product and engineering
refinement. A Spring Boot service stores signals, strategy, problems, candidate solutions, questions,
and decisions in Postgres and exposes them over MCP and a read-only GraphQL API. A connected agent does
the reasoning and editing through the [WIDE skills](skills), and a Vite + React explorer browses the
saved work.

Read the [project context](docs/project-context.md) for goals, domain decisions, and the current
checkpoint, and [AGENTS.md](AGENTS.md) before contributing. This public repository contains generic
context and synthetic examples only; keep actual signals, strategy, and organizational information
private.

## Getting started

Follow the [monorepo setup](../../README.md#getting-started) and start Docker. Run these commands
from the repository root.

Start Postgres:

```zsh
docker compose -f projects/wide/docker-compose.yml up -d --wait
```

Run service + app together:

```zsh
bazel run //projects/wide
```

Run the service only:

```zsh
bazel run //projects/wide/service
```

See the [WIDE App guide](app/README.md) for app build, run and watch commands.

### Environment variables

The run targets above activate the `local` Spring profile, whose database defaults match the compose
file, so no variables are needed for local use. Without that profile the database variables are
required.

- `WIDE_PORT`: service port, default `8095`. Set it for both the app and the service when changing
  it; the app proxies `/api/graphql` to the service.
- `WIDE_DATABASE_URL`: JDBC URL, `local` default `jdbc:postgresql://127.0.0.1:5495/wide`.
- `WIDE_DATABASE_USER`: database user, `local` default `wide`.
- `WIDE_DATABASE_PASSWORD`: database password, `local` default `wide-local`.

### Data

Records persist in the Docker volume `wide_wide-data` across `docker compose down`; `down -v`
deletes them. Flyway applies migrations at service startup. Preserve applied migrations and keep
private backups outside this repository.

### Try the loop

With synthetic content, ask your connected agent:

1. “Capture this signal in WIDE: The demo checklist could use a search box.”
2. “Set my WIDE strategy to make the fictional demo easier to learn.”
3. “Triage my WIDE collection. What should we refine next?”
4. “Use wide-refine to work through that recommendation with me.”

Refresh the explorer after agent writes to see updated records. Refinement does not authorize delivery.

## Connect an agent

The agent needs the local MCP endpoint and the [WIDE skills](skills). Keep the service running. These
commands configure clients on this machine; they do not make localhost reachable from a remote
execution environment.

### Codex

From the repository root:

```zsh
codex mcp add wide --url http://localhost:8095/mcp
mkdir -p ~/.agents/skills
for skill in wide-intake wide-triage wide-refine; do
  ln -s "$PWD/projects/wide/skills/$skill" "$HOME/.agents/skills/$skill"
done
```

The symlinks expose the repository's skills across projects without copying them. See
[Codex skill discovery](https://learn.chatgpt.com/docs/build-skills) for client-specific behavior.

### Claude Code

From the repository root:

```zsh
claude mcp add --transport http --scope user wide http://localhost:8095/mcp
mkdir -p ~/.claude/skills
for skill in wide-intake wide-triage wide-refine; do
  ln -s "$PWD/projects/wide/skills/$skill" "$HOME/.claude/skills/$skill"
done
```

These are personal skills available across local projects. See
[Claude Code skills](https://code.claude.com/docs/en/skills) for discovery and updates.

### Claude Desktop

For a local stdio connection, bridge to WIDE's HTTP endpoint using
[mcp-remote](https://github.com/punkpeye/mcp-remote) (requires Node). Merge this entry into
`~/Library/Application Support/Claude/claude_desktop_config.json`, then restart the client:

```json
{
  "mcpServers": {
    "wide": {
      "command": "npx",
      "args": ["-y", "mcp-remote", "http://localhost:8095/mcp", "--allow-http"]
    }
  }
}
```

Install the WIDE skill folders through the client's supported skill mechanism, including the
`wide-refine/references` directory. MCP configuration alone does not install skills.

After WIDE changes, restart the service, refresh MCP discovery, and update any copied or uploaded
skills. Symlinks point to the current files; start a fresh session when evaluating changed instructions.
If a skill link already exists, check its target rather than creating a nested copy.

## Tests

Run service tests:

```zsh
bazel test //projects/wide/service/...
```

Service tests that need Postgres run against throwaway Testcontainers databases, including Flyway
migrations; they never touch the local WIDE database. They are tagged `requires-docker`.

See the [WIDE App test commands](app/README.md#development) for one-off and watch runs.

Tests verify software behavior. Assess triage and refinement quality through the
[feedback checkpoint](docs/project-context.md#current-checkpoint).

## Local endpoints

- Explorer (Vite dev server): `http://localhost:5195` (proxies `/api/graphql` to the service)
- MCP streamable HTTP endpoint: `http://localhost:8095/mcp`
- GraphQL HTTP (read-only): `http://localhost:8095/graphql`
- Health: `http://localhost:8095/actuator/health`
- Postgres: `127.0.0.1:5495`, database/user `wide`, password `wide-local`

With the `local` profile the service and database listen on the loopback interface only. There is
no authentication or embedded model; WIDE needs no model API key.

## Project map

- `projects/wide/service/src/main/java/lab/wide`: Spring Boot entrypoint (`WideApplication`), with
  `api` (MCP tools and GraphQL data fetchers), `application` (use cases and transactions), `config`
  (Spring configuration), `domain` (records), and `infrastructure` (Spring Data repositories and SQL
  projections).
- `projects/wide/service/src/main/resources/schema/schema.graphqls`: GraphQL schema for the explorer.
- `projects/wide/service/src/main/resources/application-local.properties`: `local` profile with the
  compose database defaults and loopback binding.
- `projects/wide/service/src/main/resources/db/migration`: Flyway migrations.
- `projects/wide/app/src`: React explorer sources.
- `projects/wide/skills`: agent workflows for intake, triage, and refinement, plus the shared
  refinement guides the explorer also renders.
- `projects/wide/docs/project-context.md`: goals, domain decisions, API semantics, and the current
  checkpoint.

## Related docs

- `projects/wide/app/README.md`
- `projects/wide/docs/project-context.md`
- `projects/wide/AGENTS.md`
