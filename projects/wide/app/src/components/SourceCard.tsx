import { Link } from "react-router-dom";
import { getFragmentData } from "../__generated__";
import { SourceFieldsFragmentDoc } from "../__generated__/graphql";
import type { SourceLink } from "../lib/links";
import { Rationale } from "./Rationale";
import { When } from "./When";

export function SourceCard({ link }: { link: SourceLink }) {
  const signal = getFragmentData(SourceFieldsFragmentDoc, link.signal);
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
