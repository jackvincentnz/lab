<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Vite

Shared Vite build and development server defaults.

<a id="vite_build"></a>

## vite_build

```starlark
load("//tools/bazel/vite:vite.bzl", "vite_build")

vite_build(name, **kwargs)
```

Build a Vite application and declare a preview server named preview.

Runs in the calling package by default, emits the dist directory, and
prepends build to args. The preview server uses strict port allocation
and listens on all interfaces.

**PARAMETERS**

| Name                                 | Description                                          | Default Value |
| :----------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="vite_build-name"></a>name     | Target name.                                         | none          |
| <a id="vite_build-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="vite_dev_server"></a>

## vite_dev_server

```starlark
load("//tools/bazel/vite:vite.bzl", "vite_dev_server")

vite_dev_server(name, **kwargs)
```

Run Vite in the calling package by default.

Uses strict port allocation and listens on all interfaces.

**PARAMETERS**

| Name                                      | Description                                          | Default Value |
| :---------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="vite_dev_server-name"></a>name     | Target name.                                         | none          |
| <a id="vite_dev_server-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |
