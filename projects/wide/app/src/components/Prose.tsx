import Markdown from "react-markdown";
import remarkGfm from "remark-gfm";

export function Prose({ children }: { children: string }) {
  return (
    <div className="prose">
      <Markdown
        remarkPlugins={[remarkGfm]}
        skipHtml
        components={{
          // Stored content is untrusted; remote images must not load while browsing private records.
          img: ({ alt }) => <span>[Image: {alt || "not loaded"}]</span>,
          a: ({ href, children }) => (
            <a href={href} target="_blank" rel="noreferrer">
              {children}
            </a>
          ),
        }}
      >
        {children}
      </Markdown>
    </div>
  );
}
