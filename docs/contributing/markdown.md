# Markdown contribution guidelines

## Where documentation goes

| Location                | Contents                                                  |
| ----------------------- | --------------------------------------------------------- |
| `README.md`             | What a component does and how to build, run, and test it. |
| `projects/<name>/docs/` | Explanations and procedures for one project.              |
| `docs/overview/`        | What the repository is and why it works the way it does.  |
| `docs/platform/`        | Tools and workflows shared across the repository.         |
| `docs/adr/`             | Architecture decisions and their consequences.            |
| `docs/contributing/`    | Conventions for contributors.                             |

Place shared setup and workflows at the directory level that owns them. For
example, document a multi-service environment and its end-to-end tests in the
parent project README, then link to it from each app.

## Documentation site

The [documentation site](https://jack.geek.nz/lab/) publishes these locations
in place. Each folder belongs to one tab in
[`docs/website/sidebars.mjs`](../website/sidebars.mjs), so a new page appears in
its folder's tab without a sidebar change. The site build fails when a page is
in no sidebar or a link or anchor is broken. See the
[website README](../website/README.md) to preview the site.

## Writing

- Start a README with a brief description, then the commands to build, run, and
  test, with where to run them and essential details such as development URLs,
  so that readers can get it running quickly.
- Keep READMEs for similar components consistent in structure and command
  presentation, so that readers know where to look.
- Keep docs concise, so that they are quick to read and to keep current.
- Link to the document that owns a topic instead of repeating it, so that each
  fact has one place to update.
- Describe the current state, without change history or completed work, so that
  docs stay true as the code changes.
- Follow the [Markdown style](style.md#markdown-format) for lists.
