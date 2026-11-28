# Client types

Vite's client type declarations, such as `import.meta.env` and asset imports,
as a Bazel target that TypeScript projects add to their `deps`.

## Usage

```starlark
deps = ["//libs/packages/client"],
```

## Building

```zsh
bazel build //libs/packages/client
```
