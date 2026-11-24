# Documentation generation

Shared Stardoc and source-file update wrappers.

<!-- Generated with Stardoc: http://skydoc.bazel.build -->

<a id="stardoc"></a>

## stardoc

```starlark
load("//tools/bazel:docs.bzl", "stardoc")

stardoc(name, **kwargs)
```

Generate Markdown from Starlark docstrings with [Stardoc](https://github.com/bazelbuild/stardoc).

Renders labels in this repository without a repository name by default,
so load statements match the ones BUILD files use. The default templates
put each page's title first and its signatures in code blocks, so that
the documentation site can title and render the page.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="stardoc-name"></a>name |  Target name.   |  none |
| <a id="stardoc-kwargs"></a>kwargs |  Additional attributes passed to the underlying macro.   |  none |


<a id="write_source_files"></a>

## write_source_files

```starlark
load("//tools/bazel:docs.bzl", "write_source_files")

write_source_files(name, **kwargs)
```

Copy generated files into the source tree with [write_source_files](https://github.com/bazel-contrib/bazel-lib/blob/main/lib/write_source_files.bzl).

`bazel run` the target to update the files. Its diff tests fail when a
committed file differs from the generated one.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="write_source_files-name"></a>name |  Target name.   |  none |
| <a id="write_source_files-kwargs"></a>kwargs |  Additional attributes passed to the underlying macro.   |  none |


