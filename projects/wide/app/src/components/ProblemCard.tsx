import { Link } from "react-router-dom";
import { getFragmentData } from "../__generated__";
import { ProblemFieldsFragmentDoc } from "../__generated__/graphql";
import type { ProblemLink } from "../lib/links";
import { Rationale } from "./Rationale";

export function ProblemCard({
  link,
  contribution = false,
}: {
  link: ProblemLink;
  contribution?: boolean;
}) {
  const problem = getFragmentData(ProblemFieldsFragmentDoc, link.problem);
  return (
    <article className="card">
      <Link className="card-title" to={`/problems/${problem.id}`}>
        {problem.title}
      </Link>
      <p className="excerpt">{problem.description}</p>
      <Rationale contribution={contribution}>{link.rationale}</Rationale>
    </article>
  );
}
