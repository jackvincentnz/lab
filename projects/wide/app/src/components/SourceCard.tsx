import { Link } from "react-router-dom";
import { useFragment } from "../__generated__";
import { SourceFieldsFragmentDoc } from "../__generated__/graphql";
import type { SourceLink } from "../lib/links";
import { Rationale } from "./Rationale";
import { When } from "./When";

interface SourceCardProps {
  link: SourceLink;
}

export function SourceCard({ link }: SourceCardProps) {
  const signal = useFragment(SourceFieldsFragmentDoc, link.signal);
  return (
    <article className="card">
      <Link className="card-title" to={`/signals/${signal.id}`}>
        {signal.title}
      </Link>
      <div className="card-meta">
        {signal.source || "No source recorded"} ·{" "}
        <When verb="captured" value={signal.capturedAt} />
      </div>
      <p className="excerpt">{signal.content}</p>
      <details>
        <summary>Read saved content</summary>
        <div className="original">{signal.content}</div>
      </details>
      <Rationale>{link.rationale}</Rationale>
    </article>
  );
}
