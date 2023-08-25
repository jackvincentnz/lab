# Style

## Commit style

Commit style is enforced with [Commitlint](https://commitlint.js.org) on commit.

> commitlint helps your team adhering to a commit convention. By supporting npm-installed configurations it makes sharing of commit conventions easy.

This makes the commit history nice and clean, increasing the ability to quickly determine what has happened.

Commits should follow the [conventional](https://www.conventionalcommits.org/en/v1.0.0/) style. See [What is commitlint](https://github.com/conventional-changelog/commitlint/#what-is-commitlint) for more information.

## Code style

Code style is enforced with:

- [Pre-commit](https://pre-commit.com/) - Source files are linted and fixed where possible prior to commiting locally.
- [CI](https://github.com/jackvincentnz/lab/blob/main/.github/workflows/main.yml) - Source files are checked as part of continuous integration for alignment to the repo's style requirements.

### Prettier

[Prettier](https://prettier.io/) is used to format multiple source files on commit, mostly in the front end eco-system. This ensures a consistent, opinionated style.

What:

> - An opinionated code formatter
> - Supports many languages
> - Integrates with most editors
> - Has few options

Why:

> - You press save and code is formatted
> - No need to discuss style in code review
> - Saves you time and energy

See [Why prettier](https://prettier.io/docs/en/why-prettier.html) for more information.

### ESLint

[ESLint](https://eslint.org/) is used to find and fix problems in your JavaScript code.

What:

- ESLint statically analyzes your code to quickly find problems.

- It is built into most text editors and you can run ESLint as part of your continuous integration pipeline.

- It is a pluggable solution that can enforce numerous best practices and custom rules, largely against frontend source files.

Why:

- By using ESLint in the IDE, prior to commiting and during CI, we continuously provide feedback to the developer on the quality of the source code they have added.

### pre-commit

[pre-commit](https://pre-commit.com/) is used to give commit-time style feedback, so that it is not necessary to wait for a continuous integration check.

> A framework for managing and maintaining multi-language pre-commit hooks.

> Linting makes more sense when run before committing your code. By doing so you can ensure no errors go into the repository and enforce code style.

See [pre-commit](https://pre-commit.com/) for more information.
