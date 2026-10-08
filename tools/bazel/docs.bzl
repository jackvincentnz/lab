"""# Documentation generation

Generate formatted Starlark reference pages and check committed copies.
"""

load("@aspect_bazel_lib//lib:write_source_files.bzl", "write_source_files")
load("@stardoc//stardoc:stardoc.bzl", "stardoc")
load("//tools/bazel:js.bzl", "js_run_binary")

def macro_docs(name, src, deps, symbol_names):
    """Generate a Markdown page from Starlark docstrings and test its freshness.

    Formats Stardoc output with the repository's Prettier version before the
    diff test, so generation and pre-commit agree on the committed Markdown.
    Emits name, name + "_update", and name + "_update_test" targets.

    Args:
        name: Page basename, without .md.
        src: Starlark source file to document.
        deps: Starlark library targets containing the source and its loads.
        symbol_names: Public symbols to include, excluding imported APIs.
    """
    stardoc(
        name = name + "_stardoc",
        input = src,
        deps = deps,
        out = name + ".raw.md",
        func_template = "//tools/bazel/docs:func.vm",
        rule_template = "//tools/bazel/docs:rule.vm",
        render_main_repo_name = False,
        symbol_names = symbol_names,
    )

    js_run_binary(
        name = name,
        tool = "//tools/bazel/docs:format",
        srcs = [":" + name + "_stardoc"],
        args = [
            "$(rootpath :" + name + "_stardoc)",
        ],
        stdout = name + "-docgen.md",
    )

    write_source_files(
        name = name + "_update",
        files = {name + ".md": ":" + name},
        tags = ["no-coverage"],
    )

def update_docs(name, targets):
    """Update all generated pages with Aspect's write_source_files runner.

    Args:
        name: Update runner target name.
        targets: Update targets emitted by macro_docs.
    """
    write_source_files(
        name = name,
        additional_update_targets = targets,
    )
