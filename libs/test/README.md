# Test

Shared test support: `TestBase`, which Java tests extend, and the
`@RequiresDocker` annotation. Tests across the gateway, Mops, and the `ddd`
library use it. See [Testing](../../docs/contributing/testing.md) for the
conventions.

## Usage

Depend on `//libs/test/src/test/java/lab/test:test-test-lib` from test targets.

## Testing

The package holds test support only, so there are no tests to run. Build it
with:

```zsh
bazel build //libs/test/...
```
