# Java contribution guidelines

The Java language level is set in [`.bazelrc`](../../.bazelrc).

## Architecture

Follow the style of the service you change. Services with domain behavior, such
as [Mops](../../projects/mops/service), split each feature into `api`,
`application`, `domain`, and `infrastructure` packages. CRUD-style services with
little behavior may pass entities between the API and storage directly.

## Spring

- Prefer Spring Boot auto-configuration and Spring defaults, such as leaving
  `proxyBeanMethods` unset, so that the service behaves as Spring documents.
  When you replace a default, say in the PR which default fails and why.
- Put `@ConfigurationPropertiesScan` on the application class, so that
  properties records need no registration.
- Put local-only accounts and settings in an `application-local` profile that
  the Bazel run target activates, so that `bazel run` works without flags and
  the delivered image never activates them.

See [testing](testing.md) for test conventions.
