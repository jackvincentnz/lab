# pre-commit

pre-commit runs the repository's formatters and linters before a commit, while
the commit-message hook checks conventional commit messages with Commitlint.
The [hook configuration](https://github.com/jackvincentnz/lab/blob/main/.pre-commit-config.yaml)
defines the checks and their versions.

## Setup

Install pre-commit using the [official installation instructions](https://pre-commit.com/#install).
Then run this command from the repository root:

```sh
pre-commit install
```

The repository's `default_install_hook_types` setting installs `pre-commit`,
`commit-msg` and `pre-push` hooks. Hook environments are downloaded on first use.
Rerun `pre-commit install` after this setting changes, so that new hook types
are installed in an existing clone.

The `pre-push` hook runs the [Gazelle](gazelle.md) check that CI runs on the
working tree, when the pushed commits change Java, proto or Bazel files. It fails
when a BUILD file is out of date; run `aspect gazelle`, commit the changes, then
push again.

## Running checks

The hooks run automatically when committing. To check all tracked files manually:

```sh
pre-commit run --all-files
```

If a formatter changes files, review and stage those changes, then retry the
commit. Fix any remaining lint errors reported by the hooks.
