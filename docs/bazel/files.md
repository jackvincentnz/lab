# Files

Shared rules that copy and arrange files.

<!-- Generated with Stardoc: http://skydoc.bazel.build -->

<a id="copy_to_directory"></a>

## copy_to_directory

```starlark
load("//tools/bazel:files.bzl", "copy_to_directory")

copy_to_directory(name, **kwargs)
```

Copy files into one output directory with [copy_to_directory](https://github.com/bazel-contrib/bazel-lib/blob/main/docs/copy_to_directory.md).

**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="copy_to_directory-name"></a>name |  Target name.   |  none |
| <a id="copy_to_directory-kwargs"></a>kwargs |  Additional attributes passed to the underlying rule.   |  none |


