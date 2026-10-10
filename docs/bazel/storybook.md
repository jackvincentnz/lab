<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Storybook

Shared Storybook build and development server defaults.

<a id="storybook_build"></a>

## storybook_build

```starlark
load("//tools/bazel/storybook:storybook.bzl", "storybook_build")

storybook_build(name, **kwargs)
```

Build Storybook in the calling package by default.

Emits storybook-static and sets CACHE_DIR to .cache.

**PARAMETERS**

| Name                                      | Description                                          | Default Value |
| :---------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="storybook_build-name"></a>name     | Target name.                                         | none          |
| <a id="storybook_build-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="storybook_dev_server"></a>

## storybook_dev_server

```starlark
load("//tools/bazel/storybook:storybook.bzl", "storybook_dev_server")

storybook_dev_server(name, **kwargs)
```

Run Storybook on port 6006 in the calling package by default.

Sets CACHE_DIR to .cache.

**PARAMETERS**

| Name                                           | Description                                          | Default Value |
| :--------------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="storybook_dev_server-name"></a>name     | Target name.                                         | none          |
| <a id="storybook_dev_server-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |
