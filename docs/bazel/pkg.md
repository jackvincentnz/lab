# Packaging

Shared archive and directory packaging defaults.

<!-- Generated with Stardoc: http://skydoc.bazel.build -->

<a id="copy_to_directory"></a>

## copy_to_directory

```starlark
load("//tools/bazel:pkg.bzl", "copy_to_directory")

copy_to_directory(name, **kwargs)
```

Copy files into one output directory with [copy_to_directory](https://github.com/bazel-contrib/bazel-lib/blob/main/docs/copy_to_directory.md).

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="copy_to_directory-name"></a>name |  Target name.   |  none |
| <a id="copy_to_directory-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


<a id="tar"></a>

## tar

```starlark
load("//tools/bazel:pkg.bzl", "tar")

tar(name, **kwargs)
```

Package files with [pkg_tar](https://bazelbuild.github.io/rules_pkg/latest.html#pkg_tar).

Defaults to a `tar.gz` archive; override `extension` for another format.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="tar-name"></a>name |  Target name.   |  none |
| <a id="tar-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


