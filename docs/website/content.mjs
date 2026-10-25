import { readdirSync } from "node:fs";
import { createRequire } from "node:module";
import path from "node:path";
import { fileURLToPath } from "node:url";

export const repoRoot = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../..",
);

export const include = [
  "README.md",
  "CONTRIBUTING.md",
  "AGENTS.md",
  "docs/**/*.md",
  "projects/**/*.md",
  "libs/**/*.md",
  "learn/**/*.md",
  "tools/**/*.md",
];

const require = createRequire(import.meta.url);
const { GlobExcludeDefault } = require("@docusaurus/utils");

// Docusaurus's defaults leave out partials such as `_shared.md` and test files.
export const exclude = [
  ...GlobExcludeDefault,
  "**/node_modules/**",
  "**/dist/**",
  "docs/adr/template.md",
  // The site's own README explains how to run it, not the repository.
  "docs/website/**",
];

function globToRegExp(glob) {
  const pattern = glob
    .split("**/")
    .map((part) =>
      part
        .replace(/[.+^$()|[\]\\]/g, "\\$&")
        .replace(
          /\{([^}]*)\}/g,
          (_, options) => `(?:${options.split(",").join("|")})`,
        )
        .replace(/\*\*/g, "\0")
        .replace(/\*/g, "[^/]*")
        .replace(/\0/g, ".*"),
    )
    .join("(?:.*/)?");
  return new RegExp(`^${pattern}$`);
}

const includePatterns = include.map(globToRegExp);
const excludePatterns = exclude.map(globToRegExp);

// Whether a repository-relative path is a page on the site.
export function isPublished(relativePath) {
  const posixPath = relativePath.split(path.sep).join("/");
  return (
    includePatterns.some((pattern) => pattern.test(posixPath)) &&
    !excludePatterns.some((pattern) => pattern.test(posixPath))
  );
}

// Absolute paths of every published page. The walk skips symlinks, such as
// Bazel's dist/ outputs, and node_modules and dot folders, which hold no pages.
export function publishedFiles(directory = repoRoot) {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) {
      return entry.name === "node_modules" || entry.name.startsWith(".")
        ? []
        : publishedFiles(absolute);
    }
    return entry.isFile() && isPublished(path.relative(repoRoot, absolute))
      ? [absolute]
      : [];
  });
}
