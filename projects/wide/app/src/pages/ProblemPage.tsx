import { useQuery } from "@apollo/client/react";
import { Link, useParams } from "react-router-dom";
import { CandidateCard, candidateNote } from "../components/CandidateCard";
import { CopyId } from "../components/CopyId";
import { Fields } from "../components/Field";
import { Rail } from "../components/Rail";
import { RecordHeader } from "../components/RecordHeader";
import { RefinementSection } from "../components/RefinementSection";
import { Screen } from "../components/Screen";
import { SourceCard } from "../components/SourceCard";
import { Empty, Failure, Loading } from "../components/Status";
import { When } from "../components/When";
import { useSynced } from "../hooks/useSynced";
import { problemFields } from "../lib/fields";
import { count } from "../lib/format";
import { getFragmentData } from "../__generated__";
import {
  ProblemDocument,
  ProblemFieldsFragmentDoc,
} from "../__generated__/graphql";

export function ProblemPage() {
  const { id = "" } = useParams();
  const { data, loading, error, refetch } = useQuery(ProblemDocument, {
    variables: { id },
    fetchPolicy: "network-only",
  });
  const syncedAt = useSynced(data);
  const record = data?.problem;
  const problem = getFragmentData(ProblemFieldsFragmentDoc, record);
  return (
    <Screen
      crumbs={[
        <Link to="/problems">Problems</Link>,
        problem ? <span className="crumb-title">{problem.title}</span> : "…",
      ]}
      refresh={() => void refetch()}
      syncedAt={syncedAt}
      loading={loading}
    >
      {loading ? (
        <Loading />
      ) : error ? (
        <Failure retry={() => void refetch()} />
      ) : !record || !problem ? (
        <Empty>Problem not found.</Empty>
      ) : (
        <>
          <RecordHeader
            kind="problems"
            title={problem.title}
            meta={[
              <When verb="Updated" value={problem.updatedAt} />,
              count(record.signals.length, "source signal"),
              count(record.solutions.length, "candidate"),
              <CopyId id={problem.id} />,
            ]}
          />
          <div className="chain">
            <article className="record">
              <Fields spec={problemFields} record={problem} />
              <RefinementSection record={record} />
            </article>
            <Rail
              title="Sources"
              count={record.signals.length}
              empty="No source signals linked yet."
            >
              {record.signals.map((link) => (
                <SourceCard key={link.id} link={link} />
              ))}
            </Rail>
            <Rail
              title="Candidates"
              count={record.solutions.length}
              note={candidateNote}
              empty="No candidate solutions linked yet."
            >
              {record.solutions.map((link) => (
                <CandidateCard key={link.id} link={link} contribution />
              ))}
            </Rail>
          </div>
        </>
      )}
    </Screen>
  );
}
