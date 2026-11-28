# Test utils

Vitest helpers for the React apps, published in the workspace as
`@lab/test-utils`.

## Usage

Add the package to an app's test dependencies in its `BUILD.bazel`:

```starlark
test_deps = ["//:node_modules/@lab/test-utils"],
```

## Testing

```zsh
bazel test //libs/test-utils/...
```
