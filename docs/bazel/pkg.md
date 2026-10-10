<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Packaging

Shared archive packaging defaults.

<a id="tar"></a>

## tar

```starlark
load("//tools/bazel:pkg.bzl", "tar")

tar(name, **kwargs)
```

Package files with [pkg_tar](https://bazelbuild.github.io/rules_pkg/latest.html#pkg_tar).

Defaults to a tar.gz archive; override extension for another format.

**PARAMETERS**

| Name                          | Description                                          | Default Value |
| :---------------------------- | :--------------------------------------------------- | :------------ |
| <a id="tar-name"></a>name     | Target name.                                         | none          |
| <a id="tar-kwargs"></a>kwargs | Additional attributes passed to the underlying rule. | none          |
