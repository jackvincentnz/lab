<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Documentation generation

Shared Stardoc and source-file update wrappers.

<a id="stardoc"></a>

## stardoc

<pre>
load("//tools/bazel:docs.bzl", "stardoc")

stardoc(<a href="#stardoc-name">name</a>, <a href="#stardoc-kwargs">**kwargs</a>)
</pre>

Generate Markdown from Starlark docstrings with [Stardoc](https://github.com/bazelbuild/stardoc).

Renders labels in this repository without a repository name by default,
so load statements match the ones BUILD files use.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="stardoc-name"></a>name |  Target name.   |  none |
| <a id="stardoc-kwargs"></a>kwargs |  Additional attributes passed to the underlying macro.   |  none |


<a id="write_source_files"></a>

## write_source_files

<pre>
load("//tools/bazel:docs.bzl", "write_source_files")

write_source_files(<a href="#write_source_files-name">name</a>, <a href="#write_source_files-kwargs">**kwargs</a>)
</pre>

Copy generated files into the source tree with [write_source_files](https://github.com/bazel-contrib/bazel-lib/blob/main/lib/write_source_files.bzl).

`bazel run` the target to update the files. Its diff tests fail when a
committed file differs from the generated one.


**PARAMETERS**


| Name  | Description | Default Value |
| :------------- | :------------- | :------------- |
| <a id="write_source_files-name"></a>name |  Target name.   |  none |
| <a id="write_source_files-kwargs"></a>kwargs |  Additional attributes passed to the underlying macro.   |  none |


