# Java contribution guidelines

Java code targets language level 17 (see `.bazelrc`). Do not use APIs added in
later versions, such as `List.getFirst()`.

## Service layout

Use [Mops](../../projects/mops/service) as the reference service.

- Put the application class and configuration in `lab.<project>`, with
  configuration beans in a `config` package.
- Split feature code into `api` (GraphQL, REST, or MCP adapters and transport
  types), `application` (services and transactions), `domain` (entities and
  value types), and `infrastructure` (repositories, SQL, and startup tasks).
- Give each package its own Bazel target.
- A single-service project uses a flat `projects/<name>/src/{main,test}` layout,
  as [the gateway](../../projects/gateway) does.
- Pick the next free port in the 300X range for a new service. Search existing
  configuration for ports in use before choosing.
- Add only the Maven artifacts the change uses. Add `springboot` or image
  targets when the issue asks for packaging.

## Spring

- Prefer Spring Boot auto-configuration and Spring Security defaults. When you
  replace a default, say in the PR which default fails and why.
- Do not set `proxyBeanMethods`. Put `@ConfigurationPropertiesScan` on the
  application class.
- Use YAML for new configuration.
- Put local-only accounts and settings in an `application-local` file. Activate
  the profile with `SPRING_PROFILES_ACTIVE=local` in the Bazel run target's
  `env`. Never activate a profile from the base configuration file; the
  delivered image runs with no profile.
- Return `Optional` from public methods and configuration records instead of
  `null`. Validate record constructors with Spring `Assert`.
- Return 401 only when the service checked a caller-supplied credential and
  rejected it. A failure to evaluate the credential, such as an unreachable key
  endpoint, is a 500.

## Persistence

- Use Spring Data default table mapping and standard repositories. Add
  `@Table`, custom repository implementations, or hand-written SQL only when
  the defaults cannot express the query.
- Use database-generated UUID primary keys. Never make a caller supply an ID.
- Until a service stores data someone relies on, edit its first migration
  instead of adding new ones. After that, never edit an applied migration.

See [testing](testing.md) for test conventions.
