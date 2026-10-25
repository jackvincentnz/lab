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

const isInside = (directory, file) =>
  !path.relative(directory, file).startsWith("..");

function folders(page, field) {
  const value = page.frontMatter[field] ?? [];
  if (!Array.isArray(value)) {
    throw new Error(
      `${page.repoPath} sets \`${field}\` to ${JSON.stringify(value)}; list folders instead, such as [libs/test].`,
    );
  }
  return [...new Set(value)];
}

// Reads each relation once, where it is written, and inverts it: `uses` in a
// README's front matter gives "Used by" on the other README, and `affects` in
// a decision record gives "Decisions" on each README it names.
async function loadConnections(repoRoot, decisionsDir) {
  const pages = new Map();
  for (const file of publishedFiles()) {
    if (path.basename(file) !== "README.md" && !isInside(decisionsDir, file)) {
      continue;
    }
    const { frontMatter, content } = await DEFAULT_PARSE_FRONT_MATTER({
      filePath: file,
      fileContent: readFileSync(file, "utf8"),
    });
    const repoPath = toPosix(path.relative(repoRoot, file));
    pages.set(repoPath, {
      file,
      repoPath,
      frontMatter,
      title:
        frontMatter.title ?? parseMarkdownContentTitle(content).contentTitle,
    });
  }

  const connections = new Map();
  const connectionsOf = (page) => {
    if (!connections.has(page.file)) {
      connections.set(page.file, {
        uses: [],
        usedBy: [],
        decisions: [],
        affects: [],
      });
    }
    return connections.get(page.file);
  };
  const readmeOf = (folder, page, field) => {
    const readme = pages.get(`${folder}/README.md`);
    if (!readme) {
      throw new Error(
        `${page.repoPath} lists ${folder} in \`${field}\`, which has no published README.md.`,
      );
    }
    if (readme === page) {
      throw new Error(`${page.repoPath} lists itself in \`${field}\`.`);
    }
    return readme;
  };

  for (const page of pages.values()) {
    const isDecision = isInside(decisionsDir, page.file);
    const field = isDecision ? "affects" : "uses";
    for (const folder of folders(page, field)) {
      const readme = readmeOf(folder, page, field);
      if (isDecision) {
        connectionsOf(page).affects.push(readme);
        connectionsOf(readme).decisions.push(page);
      } else {
        connectionsOf(page).uses.push(readme);
        connectionsOf(readme).usedBy.push(page);
      }
    }
  }
  return connections;
}

const text = (value) => ({ type: "text", value });

// Links to each page by title, in title order, with a decision's status.
function links(pages, fromFile) {
  const nodes = [];
  for (const page of pages.toSorted((a, b) => a.title.localeCompare(b.title))) {
    if (nodes.length > 0) {
      nodes.push(text(", "));
    }
    nodes.push({
      type: "link",
      url: toPosix(path.relative(path.dirname(fromFile), page.file)),
      children: [text(page.title)],
    });
    if (page.frontMatter.status) {
      nodes.push(text(` (${page.frontMatter.status})`));
    }
  }
  return nodes;
}

function addConnectionsBox(tree, title, connections, file) {
  const items = [
    ["Uses", connections.uses],
    ["Used by", connections.usedBy],
    ["Decisions", connections.decisions],
  ]
    .filter(([, pages]) => pages.length > 0)
    .map(([label, pages]) => ({
      type: "listItem",
      children: [
        {
          type: "paragraph",
          children: [
            { type: "strong", children: [text(label)] },
            text(": "),
            ...links(pages, file.path),
          ],
        },
      ],
    }));
  if (items.length === 0) {
    return;
  }

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
}

// Adds the folders a decision record affects to its status line, written by
// remark-decision-status, which runs first.
function addAffects(tree, title, affects, file) {
  if (affects.length === 0) {
    return;
  }
  const affected = [text("Affects: "), ...links(affects, file.path)];
  const statusLine = tree.children.find((node) => node.data?.decisionStatus);
  if (statusLine) {
    statusLine.children.push(text(" · "), ...affected);
  } else {
    tree.children.splice(title + 1, 0, {
      type: "paragraph",
      children: affected,
    });
  }
}

// Shows each README's connections in a box after its introduction, and each
// decision record's affected folders on its status line.
export default function remarkConnections({ repoRoot, decisionsDir }) {
  let loaded;
  return async (tree, file) => {
    // Forget a failed load, so that fixing the typo is enough to recover.
    loaded ??= loadConnections(repoRoot, decisionsDir).catch((error) => {
      loaded = undefined;
      throw error;
    });
    const connections = (await loaded).get(file.path);
    if (!connections) {
      return;
    }

    const title = tree.children.findIndex(
      (node) => node.type === "heading" && node.depth === 1,
    );
    addConnectionsBox(tree, title, connections, file);
    addAffects(tree, title, connections.affects, file);
  };
}
