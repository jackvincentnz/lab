# pre-commit

pre-commit runs the repository's formatters and linters before a commit and a
Gazelle check before a push, while the commit-message hook checks conventional
commit messages with Commitlint.
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

The `pre-push` hook runs the [Gazelle](gazelle.md) check that CI runs on your
committed and staged files, when the pushed commits add or change Java, proto,
Bazel or Maven lock files. It fails when a BUILD file is out of date; run
`aspect gazelle`, commit the changes, then push again. A push that only deletes
files skips the hook, so run `aspect gazelle` yourself after you delete a Java
file.

## Running checks

The hooks run automatically when committing and pushing. To check all tracked
files manually:

```sh
pre-commit run --all-files
```

If a formatter changes files, review and stage those changes, then retry the
commit. Fix any remaining lint errors reported by the hooks.
