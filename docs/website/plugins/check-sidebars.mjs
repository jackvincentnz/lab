function collectDocIds(items, ids) {
  for (const item of items) {
    if (item.type === "doc" || item.type === "ref") {
      ids.add(item.id);
    }
    if (item.type === "category") {
      if (item.link?.type === "doc") {
        ids.add(item.link.id);
      }
      collectDocIds(item.items, ids);
    }
  }
  return ids;
}

// Fails the build when a page is in no sidebar, because readers can only reach
// such a page through a direct link.
export default function checkSidebars() {
  return {
    name: "check-sidebars",
    async allContentLoaded({ allContent }) {
      const { loadedVersions } =
        allContent["docusaurus-plugin-content-docs"].default;
      for (const version of loadedVersions) {
        const ids = new Set();
        for (const sidebar of Object.values(version.sidebars)) {
          collectDocIds(sidebar, ids);
        }
        const missing = version.docs
          .filter((doc) => !ids.has(doc.id))
          .map((doc) => doc.source);
        if (missing.length > 0) {
          throw new Error(
            `These pages are in no sidebar. Add their folder to docs/website/sidebars.mjs:\n${missing.join("\n")}`,
          );
        }
      }
    },
  };
}
