import { Link } from "react-router-dom";
import { useFragment } from "../__generated__";
import { CandidateFieldsFragmentDoc } from "../__generated__/graphql";
import type { SolutionLink } from "../lib/links";
import { Rationale } from "./Rationale";

export const candidateNote =
  "Candidates may be alternatives or complementary. Saved drafts do not establish readiness.";

interface CandidateCardProps {
  link: SolutionLink;
  contribution?: boolean;
}

export function CandidateCard({
  link,
  contribution = false,
}: CandidateCardProps) {
  const solution = useFragment(CandidateFieldsFragmentDoc, link.solution);
  return (
    <article className="card">
      <Link className="card-title" to={`/solutions/${solution.id}`}>
        {solution.title}
      </Link>
      <p className="excerpt">{solution.approach}</p>
      <Rationale contribution={contribution}>{link.rationale}</Rationale>
    </article>
  );
}
