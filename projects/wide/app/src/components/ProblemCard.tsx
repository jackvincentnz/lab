import { Link } from "react-router-dom";
import { useFragment } from "../__generated__";
import { ProblemFieldsFragmentDoc } from "../__generated__/graphql";
import type { ProblemLink } from "../lib/links";
import { Rationale } from "./Rationale";

interface ProblemCardProps {
  link: ProblemLink;
  contribution?: boolean;
}

export function ProblemCard({ link, contribution = false }: ProblemCardProps) {
  const problem = useFragment(ProblemFieldsFragmentDoc, link.problem);
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
