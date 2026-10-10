import { useQuery } from "@apollo/client/react";
import { Link, useParams } from "react-router-dom";
import { CopyId } from "../components/CopyId";
import { Fields } from "../components/Field";
import { ProblemCard } from "../components/ProblemCard";
import { Rail } from "../components/Rail";
import { RecordHeader } from "../components/RecordHeader";
import { RefinementSection } from "../components/RefinementSection";
import { Screen } from "../components/Screen";
import { SourceCard } from "../components/SourceCard";
import { Empty, Failure, Loading } from "../components/Status";
import { When } from "../components/When";
import { useSynced } from "../hooks/useSynced";
import { candidateFields } from "../lib/fields";
import { count } from "../lib/format";
import { getFragmentData } from "../__generated__";
import {
  CandidateFieldsFragmentDoc,
  SolutionDocument,
} from "../__generated__/graphql";

export function SolutionPage() {
  const { id = "" } = useParams();
  const { data, loading, error, refetch } = useQuery(SolutionDocument, {
    variables: { id },
    fetchPolicy: "network-only",
  });
  const syncedAt = useSynced(data);
  const record = data?.solution;
  const solution = getFragmentData(CandidateFieldsFragmentDoc, record);
  return (
    <Screen
      crumbs={[
        <Link to="/solutions">Candidates</Link>,
        solution ? <span className="crumb-title">{solution.title}</span> : "…",
      ]}
      refresh={() => void refetch()}
      syncedAt={syncedAt}
      loading={loading}
    >
      {loading ? (
        <Loading />
      ) : error ? (
        <Failure retry={() => void refetch()} />
      ) : !record || !solution ? (
        <Empty>Candidate not found.</Empty>
      ) : (
        <>
          <RecordHeader
            kind="solutions"
            title={solution.title}
            meta={[
              <When verb="Updated" value={solution.updatedAt} />,
              count(record.problems.length, "problem"),
              count(record.signals.length, "source signal"),
              <CopyId id={solution.id} />,
            ]}
          />
          <div className="chain">
            <article className="record">
              <Fields spec={candidateFields} record={solution} />
              <RefinementSection record={record} />
            </article>
            <Rail
              title="Problems"
              count={record.problems.length}
              empty={
                <>
                  <strong>Problem not yet linked</strong>
                  <p>
                    This draft needs a clear problem relationship before a
                    delivery decision.
                  </p>
                </>
              }
            >
              {record.problems.map((link) => (
                <ProblemCard key={link.id} link={link} contribution />
              ))}
            </Rail>
            <Rail
              title="Sources"
              count={record.signals.length}
              empty="No source signals linked yet."
            >
              {record.signals.map((link) => (
                <SourceCard key={link.id} link={link} />
              ))}
            </Rail>
          </div>
        </>
      )}
    </Screen>
  );
}
