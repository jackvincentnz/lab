# Documentation site

[Docusaurus](https://docusaurus.io) site published at
[jack.geek.nz/lab](https://jack.geek.nz/lab/). It reads the repository's
Markdown in place; see [where documentation goes](../contributing/markdown.md)
for which folders it publishes.

## Getting started

Install dependencies, then start the development server at
`http://localhost:3000/lab/`, which reloads as pages change. It watches only the
pages that existed when it started, so restart it after adding a page or
changing a `_category_.json`, `attached-docs.mjs`, `content.mjs`, or a plugin:

```zsh
cd docs/website
pnpm install
pnpm start
```

Build the site and serve the build, which checks links, anchors, and that every
page is in a sidebar:

```zsh
pnpm build
pnpm serve
```

The [Docs workflow](../../.github/workflows/docs.yml) builds the site on each pull
request and deploys it to GitHub Pages from `main`.
