import { readFileSync } from "node:fs";
import { createRequire } from "node:module";
import path from "node:path";
import { publishedFiles } from "../content.mjs";

const require = createRequire(import.meta.url);
const {
  DEFAULT_PARSE_FRONT_MATTER,
  parseMarkdownContentTitle,
} = require("@docusaurus/utils");

const toPosix = (filePath) => filePath.split(path.sep).join("/");

// Reads each relation once, where it is written, and inverts it: `uses` in a
// README's front matter gives "Used by" on the other README, and `affects` in
// a decision record gives "Decisions" on each README it names.
async function loadConnections(repoRoot, decisionsDir) {
  const pages = new Map();
  for (const file of publishedFiles()) {
    const { frontMatter, content } = await DEFAULT_PARSE_FRONT_MATTER({
      filePath: file,
      fileContent: readFileSync(file, "utf8"),
    });
    pages.set(toPosix(path.relative(repoRoot, file)), {
      file,
      frontMatter,
      title: parseMarkdownContentTitle(content).contentTitle,
    });
  }

  const connections = new Map();
  const readmeOf = (folder, source, field) => {
    const readme = pages.get(`${folder}/README.md`);
    if (!readme) {
      throw new Error(
        `${source} lists ${folder} in \`${field}\`, which has no published README.md.`,
      );
    }
    return readme;
  };
  const connectionsOf = (readme) => {
    if (!connections.has(readme.file)) {
      connections.set(readme.file, { uses: [], usedBy: [], decisions: [] });
    }
    return connections.get(readme.file);
  };

  for (const [repoPath, page] of pages) {
    const { uses = [], affects = [] } = page.frontMatter;
    for (const folder of uses) {
      const used = readmeOf(folder, repoPath, "uses");
      connectionsOf(page).uses.push(used);
      connectionsOf(used).usedBy.push(page);
    }
    if (!path.relative(decisionsDir, page.file).startsWith("..")) {
      for (const folder of affects) {
        connectionsOf(readmeOf(folder, repoPath, "affects")).decisions.push(
          page,
        );
      }
    }
  }
  return connections;
}

const text = (value) => ({ type: "text", value });

function listItem(label, pages, fromFile) {
  const links = pages.flatMap((page, index) => [
    ...(index > 0 ? [text(", ")] : []),
    {
      type: "link",
      url: toPosix(path.relative(path.dirname(fromFile), page.file)),
      children: [text(page.title)],
    },
    ...(page.frontMatter.status ? [text(` (${page.frontMatter.status})`)] : []),
  ]);
  return {
    type: "listItem",
    children: [
      {
        type: "paragraph",
        children: [
          { type: "strong", children: [text(label)] },
          text(": "),
          ...links,
        ],
      },
    ],
  };
}

// Adds a Connections box after a README's introduction, listing what it uses,
// what uses it, and the decisions about it.
export default function remarkConnections({ repoRoot, decisionsDir }) {
  let loaded;
  return async (tree, file) => {
    loaded ??= loadConnections(repoRoot, decisionsDir);
    const connections = (await loaded).get(file.path);
    if (!connections) {
      return;
    }

    const items = [
      ["Uses", connections.uses],
      ["Used by", connections.usedBy],
      ["Decisions", connections.decisions],
    ]
      .filter(([, pages]) => pages.length > 0)
      .map(([label, pages]) =>
        listItem(
          label,
          pages.toSorted((a, b) => a.title.localeCompare(b.title)),
          file.path,
        ),
      );

    const title = tree.children.findIndex(
      (node) => node.type === "heading" && node.depth === 1,
    );
    // At the end of the introduction, before the first section.
    const section = tree.children.findIndex(
      (node, index) => index > title && node.type === "heading",
    );
    tree.children.splice(section === -1 ? tree.children.length : section, 0, {
      type: "containerDirective",
      name: "info",
      children: [
        {
          type: "paragraph",
          data: { directiveLabel: true },
          children: [text("Connections")],
        },
        { type: "list", ordered: false, spread: false, children: items },
      ],
    });
  };
}
