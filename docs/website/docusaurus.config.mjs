import { exclude, include, isPublished, repoRoot } from "./content.mjs";
import checkSidebars from "./plugins/check-sidebars.mjs";
import docsInPlace from "./plugins/docs-in-place.mjs";
import remarkRepoLinks from "./plugins/remark-repo-links.mjs";
import sidebarItems from "./plugins/sidebar-items.mjs";

const githubUrl = "https://github.com/jackvincentnz/lab";

const tab = (sidebarId, label) => ({
  type: "docSidebar",
  sidebarId,
  label,
  position: "left",
});

export default {
  title: "Lab",
  tagline: "A Bazel monorepo for Java services and React apps",
  url: "https://jack.geek.nz",
  baseUrl: "/lab/",
  trailingSlash: false,
  onBrokenLinks: "throw",
  onBrokenAnchors: "throw",
  markdown: {
    // CommonMark, as GitHub renders it, so that a `{` or an unclosed HTML tag
    // in everyday Markdown doesn't fail the build the way MDX would.
    format: "md",
    mermaid: true,
    hooks: {
      onBrokenMarkdownLinks: "throw",
      onBrokenMarkdownImages: "throw",
    },
  },
  themes: [
    "@docusaurus/theme-mermaid",
    [
      "@easyops-cn/docusaurus-search-local",
      {
        docsRouteBasePath: "/",
        indexBlog: false,
        indexPages: false,
        // Hashing scans every Markdown file under docsDir, and the docs live
        // across the repository root, Bazel's output symlinks included.
        hashed: false,
      },
    ],
  ],
  plugins: [
    [
      docsInPlace,
      {
        // Docs stay next to the code they describe; the site reads them in place.
        path: repoRoot,
        include,
        exclude,
        routeBasePath: "/",
        sidebarPath: "./sidebars.mjs",
        sidebarItemsGenerator: sidebarItems,
        beforeDefaultRemarkPlugins: [
          [remarkRepoLinks, { repoRoot, isPublished, githubUrl }],
        ],
        // A string editUrl would include the docs path relative to this site,
        // so build the URL from the repository-relative path.
        editUrl: ({ docPath }) => `${githubUrl}/edit/main/${docPath}`,
        showLastUpdateTime: true,
      },
    ],
    checkSidebars,
  ],
  presets: [["classic", { blog: false, pages: false, docs: false }]],
  themeConfig: {
    colorMode: { respectPrefersColorScheme: true },
    navbar: {
      title: "Lab",
      items: [
        tab("overview", "Overview"),
        tab("projects", "Projects"),
        tab("platform", "Platform"),
        tab("decisions", "Decisions"),
        tab("contribute", "Contribute"),
        { href: githubUrl, label: "GitHub", position: "right" },
      ],
    },
    docs: { sidebar: { hideable: true } },
  },
};
