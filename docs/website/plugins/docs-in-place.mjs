import { createRequire } from "node:module";
import { publishedFiles } from "../content.mjs";

const require = createRequire(import.meta.url);
const {
  default: pluginContentDocs,
  validateOptions,
} = require("@docusaurus/plugin-content-docs");

// The docs plugin, reading pages in place across the repository. Its default
// watch globs start at the docs path, the repository root, so the development
// server would walk Bazel's output symlinks and every node_modules until it ran
// out of memory. Watch the published pages and plain files such as the sidebar
// definition instead; a new page appears after the server restarts.
export default async function docsInPlace(context, options) {
  const plugin = await pluginContentDocs(context, options);
  return {
    ...plugin,
    getPathsToWatch() {
      const files = plugin
        .getPathsToWatch()
        .filter((watched) => !watched.includes("*"));
      return [...files, ...publishedFiles()];
    },
  };
}

docsInPlace.validateOptions = validateOptions;
