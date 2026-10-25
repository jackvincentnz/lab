import Link from "@docusaurus/Link";
import { useAllDocsData } from "@docusaurus/plugin-content-docs/client";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import Layout from "@theme/Layout";
import styles from "./index.module.css";

const TABS = [
  {
    sidebar: "projects",
    title: "Projects",
    description:
      "The services and apps built here, the libraries they share, and smaller experiments. The Projects tab lists them all.",
  },
  {
    sidebar: "platform",
    title: "Platform",
    description:
      "How the repository builds, tests, and checks itself, from Bazel to coverage and development tools.",
  },
  {
    sidebar: "decisions",
    title: "Decisions",
    description:
      "Architecture decision records: what was decided, the options considered, and why.",
  },
  {
    sidebar: "contribute",
    title: "Contribute",
    description:
      "The workflow for making a change, and the conventions for each language and tool.",
  },
];

export default function Home() {
  const { siteConfig } = useDocusaurusContext();
  // Each tab's first page, so the cards follow the sidebars.
  const { sidebars } = useAllDocsData().default.versions[0];

  return (
    <Layout description={siteConfig.tagline}>
      <header className="hero hero--primary">
        <div className="container">
          <h1 className="hero__title">{siteConfig.title}</h1>
          <p className="hero__subtitle">{siteConfig.tagline}</p>
          <div className={styles.actions}>
            <Link
              className="button button--secondary button--lg"
              to={sidebars.overview.link.path}
            >
              Read the introduction
            </Link>
            <Link
              className="button button--outline button--secondary button--lg"
              href="https://github.com/jackvincentnz/lab"
            >
              View on GitHub
            </Link>
          </div>
        </div>
      </header>
      <main className="container margin-vert--xl">
        <div className="row">
          {TABS.map((tab) => (
            <div key={tab.sidebar} className="col col--3 margin-bottom--lg">
              <Link
                className={`card padding--lg ${styles.card}`}
                to={sidebars[tab.sidebar].link.path}
              >
                <h2>{tab.title}</h2>
                <p>{tab.description}</p>
              </Link>
            </div>
          ))}
        </div>
      </main>
    </Layout>
  );
}
