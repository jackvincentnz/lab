<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Vitest

Shared Vitest run and watch defaults.

<a id="vitest_run"></a>

## vitest_run

```starlark
load("//tools/bazel/vitest:vitest.bzl", "vitest_run")

vitest_run(name, **kwargs)
```

Run cacheable Vitest tests in the calling package by default.

Prepends run to args and defaults the test size to small.

**PARAMETERS**

| Name                                 | Description                                          | Default Value |
| :----------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="vitest_run-name"></a>name     | Target name.                                         | none          |
| <a id="vitest_run-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |

<a id="vitest_watch"></a>

## vitest_watch

```starlark
load("//tools/bazel/vitest:vitest.bzl", "vitest_watch")

vitest_watch(name, **kwargs)
```

Run Vitest in watch mode in the calling package by default.

Prepends watch to args; pass --ui in args to open the test UI.

**PARAMETERS**

| Name                                   | Description                                          | Default Value |
| :------------------------------------- | :--------------------------------------------------- | :------------ |
| <a id="vitest_watch-name"></a>name     | Target name.                                         | none          |
| <a id="vitest_watch-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |
