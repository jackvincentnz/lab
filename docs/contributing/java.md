# Java

The Java language level is set in [`.bazelrc`](../../.bazelrc).

## Architecture

Follow the style of the service you change. Services with domain behavior, such
as [Mops](../../projects/mops/service), split each feature into `api`,
`application`, `domain`, and `infrastructure` packages. CRUD-style services with
little behavior may pass entities between the API and storage directly.

## Naming

- Name a variable, field, or parameter after its type in camelCase, such as
  `budgetRepository` for a `BudgetRepository` and `jdbcTemplate` for a
  `JdbcTemplate`, so that readers see what it is without looking it up.
  Shorten only where the type repeats the enclosing context, such as `id` for
  the `LineItemId` field of `LineItem`, and never to an abbreviation.

## Classes

- Give each class one role and name it for that role, such as a `WebFilter` or
  a Kafka listener in a class of its own, so that readers find behavior by its
  name.

## Layout

- Order a class top down, following Clean Code's stepdown rule: put each private
  method below the first method that calls it, so that details appear as a
  reader needs them.
- Write a method as a sequence of short steps separated by blank lines, so that
  its shape is visible at a glance. See [testing](testing.md#layout) for how this
  applies to tests.

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
