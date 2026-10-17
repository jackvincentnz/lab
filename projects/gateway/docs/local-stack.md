# Local stack

`bazel run //projects/gateway:start` is a `rules_multirun` target that runs two things in
parallel: the gateway, and the `//projects/mops` multirun, which itself runs the Mops service and
the Mops app dev server. Everything shares one terminal, and Ctrl-C stops all of it.

| Process      | Port | Started by                           |
| ------------ | ---- | ------------------------------------ |
| Gateway      | 3006 | `//projects/gateway`                 |
| Redis        | 6379 | The gateway, from `compose.yaml`     |
| Mops service | 8080 | `//projects/mops`                    |
| Mops app     | 5173 | `//projects/mops`, which opens a tab |

Open `http://localhost:3006`. The tab the Mops app opens at `http://127.0.0.1:5173` bypasses the
gateway, so requests from it carry no identity token and are rejected.

## Redis

The gateway's run target depends on Spring Boot's Docker Compose support, which runs
`docker compose up` for `compose.yaml` before the application context starts, waits for the
container to accept connections, and points Spring Session at the port the container publishes.
On shutdown it runs `docker compose stop`, so the container is kept and reused by the next run.

The host port is `GATEWAY_REDIS_PORT`, default 6379. Docker Compose reads it from the gateway's
environment, and Spring connects to whatever port the container publishes, so no Spring property
changes with it.

`bazel run` does not run from the project directory, so the run target passes the compose file as
a runfile through `spring.docker.compose.file`. The dependency sits on the run target only; the
library a delivered image would be built from does not carry it, and Spring skips the integration
in tests.

## Mops identity

The stack sets `MOPS_IDENTITY_DEVELOPMENT_ENABLED=false` for the Mops multirun. Mops still runs
with its `local` profile, but a request that reaches it without a token fails instead of being
served as the development identity, so a route that misses the gateway shows up as an error rather
than as working. Running `bazel run //projects/mops` on its own keeps the development identity.
