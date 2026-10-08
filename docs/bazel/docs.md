<!-- Generated with Stardoc: http://skydoc.bazel.build -->

# Documentation generation

Generate formatted Starlark reference pages and check committed copies.

<a id="macro_docs"></a>

## macro_docs

```starlark
load("//tools/bazel:docs.bzl", "macro_docs")

macro_docs(name, src, deps, symbol_names)
```

Generate a Markdown page from Starlark docstrings and test its freshness.

Formats Stardoc output with the repository's Prettier version before the
diff test, so generation and pre-commit agree on the committed Markdown.
Emits name, name + "_update", and name + "_update_test" targets.

**PARAMETERS**

| Name                                             | Description                                                   | Default Value |
| :----------------------------------------------- | :------------------------------------------------------------ | :------------ |
| <a id="macro_docs-name"></a>name                 | Page basename, without .md.                                   | none          |
| <a id="macro_docs-src"></a>src                   | Starlark source file to document.                             | none          |
| <a id="macro_docs-deps"></a>deps                 | Starlark library targets containing the source and its loads. | none          |
| <a id="macro_docs-symbol_names"></a>symbol_names | Public symbols to include, excluding imported APIs.           | none          |

<a id="update_docs"></a>

## update_docs

```starlark
load("//tools/bazel:docs.bzl", "update_docs")

update_docs(name, targets)
```

Update all generated pages with Aspect's write_source_files runner.

**PARAMETERS**

| Name                                    | Description                           | Default Value |
| :-------------------------------------- | :------------------------------------ | :------------ |
| <a id="update_docs-name"></a>name       | Update runner target name.            | none          |
| <a id="update_docs-targets"></a>targets | Update targets emitted by macro_docs. | none          |
