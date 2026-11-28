# Packaging

Shared archive packaging defaults.

<!-- Generated with Stardoc: http://skydoc.bazel.build -->

<a id="files"></a>

## files

```starlark
load("//tools/bazel:pkg.bzl", "files")

files(name, **kwargs)
```

Place files in an archive with [pkg_files](https://bazelbuild.github.io/rules_pkg/latest.html#pkg_files).

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="files-name"></a>name |  Target name.   |  none |
| <a id="files-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


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


<a id="zip"></a>

## zip

```starlark
load("//tools/bazel:pkg.bzl", "zip")

zip(name, **kwargs)
```

Package files with [pkg_zip](https://bazelbuild.github.io/rules_pkg/latest.html#pkg_zip).

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="zip-name"></a>name |  Target name.   |  none |
| <a id="zip-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


