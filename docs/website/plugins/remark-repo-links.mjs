import { existsSync, statSync } from "node:fs";
import path from "node:path";

// Docs link to repository files that are not pages, such as BUILD files,
// workflows, and folders. Point those links at GitHub, so that they work on
// the site as they do when reading the Markdown on GitHub.
export default function remarkRepoLinks({ repoRoot, isPublished, githubUrl }) {
  function rewrite(url, file) {
    if (!url || /^([a-z][a-z0-9+.-]*:|#|\/)/i.test(url)) {
      return url;
    }

    const [target, ...anchor] = url.split("#");
    const hash = anchor.length ? `#${anchor.join("#")}` : "";
    let decoded;
    try {
      decoded = decodeURI(target);
    } catch {
      // A malformed escape, such as a bare %, is not a repository path.
      return url;
    }
    const absolute = path.resolve(path.dirname(file.path), decoded);
    const relative = path.relative(repoRoot, absolute);
    if (relative.startsWith("..") || !existsSync(absolute)) {
      return url;
    }

    const repoPath = relative.split(path.sep).join("/");
    if (statSync(absolute).isDirectory()) {
      const readme = path.join(relative, "README.md");
      if (isPublished(readme) && existsSync(path.join(repoRoot, readme))) {
        return (
          path.relative(path.dirname(file.path), path.join(repoRoot, readme)) +
          hash
        );
      }
      return `${githubUrl}/tree/main/${repoPath}${hash}`;
    }

    return isPublished(relative)
      ? url
      : `${githubUrl}/blob/main/${repoPath}${hash}`;
  }

  function visit(node, file) {
    if (node.type === "link" || node.type === "definition") {
      node.url = rewrite(node.url, file);
    }
    for (const child of node.children ?? []) {
      visit(child, file);
    }
  }

  return (tree, file) => visit(tree, file);
}
