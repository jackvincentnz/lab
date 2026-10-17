"""# Documentation generation

Shared Stardoc and source-file update wrappers.
"""

load("@aspect_bazel_lib//lib:write_source_files.bzl", _write_source_files = "write_source_files")
load("@stardoc//stardoc:stardoc.bzl", _stardoc = "stardoc")

def stardoc(name, **kwargs):
    """Generate Markdown from Starlark docstrings with [Stardoc](https://github.com/bazelbuild/stardoc).

    Renders labels in this repository without a repository name by default,
    so load statements match the ones BUILD files use.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying macro.
    """
    _stardoc(
        name = name,
        render_main_repo_name = kwargs.pop("render_main_repo_name", False),
        **kwargs
    )

def write_source_files(name, **kwargs):
    """Copy generated files into the source tree with [write_source_files](https://github.com/bazel-contrib/bazel-lib/blob/main/lib/write_source_files.bzl).

    `bazel run` the target to update the files. Its diff tests fail when a
    committed file differs from the generated one.

    Args:
        name: Target name.
        **kwargs: Additional attributes passed to the underlying macro.
    """
    _write_source_files(
        name = name,
        **kwargs
    )
