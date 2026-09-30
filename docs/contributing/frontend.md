# Frontend contribution guidelines

Use the [Mops app](../../projects/mops/app) as the reference application.

## Apps

- Declare each app with `fe_app` from `//tools/bazel:fe.bzl`, one flattened
  Vite application per Bazel package.
- Give a full-stack project a default `bazel run //projects/<name>` target that
  runs the service and app together with `multirun`, as Mops does.
- Add each new project as a component in `codecov.yml`.
- Put non-code assets that Vite or TypeScript import in a `js_library`, not a
  `filegroup`.
- Put test helpers in the app's existing `src/test` setup instead of in test
  files.

## GraphQL

- Keep the schema in the service and generate client types with
  `graphql_codegen` from `//tools/bazel:graphql.bzl` and the client preset.
- Put each `.gql` document next to the component that uses it.
- Model API data as schema types, unions, and enums. Do not return serialized
  JSON for the client to parse.
- Treat generated types as boundary types. Map query results to component
  props at the edge, and prefer inference to importing operation types.
- Compare enums with wire string literals.

## Changes

- Keep helpers minimal. Do not add guards, types, or exports the change does
  not need, and name each helper after the value it takes.
- Keep upgrade diffs minimal. Remove props, helpers, and styles left over from
  debugging before you open the PR.
- Check a UI change in the running app, including the browser console, before
  reporting it as done.
