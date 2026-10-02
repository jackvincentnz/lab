import { useQuery } from "@apollo/client/react";
import { Link, useParams } from "react-router-dom";
import { CandidateCard, candidateNote } from "../components/CandidateCard";
import { Clamp } from "../components/Clamp";
import { CopyId } from "../components/CopyId";
import { ProblemCard } from "../components/ProblemCard";
import { Rail } from "../components/Rail";
import { RecordHeader } from "../components/RecordHeader";
import { Screen } from "../components/Screen";
import { Empty, Failure, Loading } from "../components/Status";
import { When } from "../components/When";
import { useSynced } from "../hooks/useSynced";
import { count } from "../lib/format";
import { getFragmentData } from "../__generated__";
import {
  SignalDocument,
  SourceFieldsFragmentDoc,
} from "../__generated__/graphql";

export function SignalPage() {
  const { id = "" } = useParams();
  const { data, loading, error, refetch } = useQuery(SignalDocument, {
    variables: { id },
    fetchPolicy: "network-only",
  });
  const syncedAt = useSynced(data);
  const record = data?.signal;
  const signal = getFragmentData(SourceFieldsFragmentDoc, record);
  return (
    <Screen
      crumbs={[
        <Link to="/signals">Signals</Link>,
        signal ? <span className="crumb-title">{signal.title}</span> : "…",
      ]}
      refresh={() => void refetch()}
      syncedAt={syncedAt}
      loading={loading}
    >
      {loading ? (
        <Loading />
      ) : error ? (
        <Failure retry={() => void refetch()} />
      ) : !record || !signal ? (
        <Empty>Signal not found.</Empty>
      ) : (
        <>
          <RecordHeader
            kind="signals"
            title={signal.title}
            meta={[
              <When verb="Captured" value={signal.capturedAt} />,
              count(record.problems.length, "problem"),
              count(record.solutions.length, "candidate"),
              <CopyId id={signal.id} />,
            ]}
          />
          <div className="chain">
            <article className="record">
              <section className="field">
                <h2 className="label">Source</h2>
                {signal.source ? (
                  <Clamp>{signal.source}</Clamp>
                ) : (
                  <p className="dim">No source recorded</p>
                )}
              </section>
              <section className="field">
                <h2 className="label">Saved content</h2>
                <div className="original">{signal.content}</div>
              </section>
            </article>
            <Rail
              title="Problems"
              count={record.problems.length}
              empty="No problem linked yet. The need remains to be clarified."
            >
              {record.problems.map((link) => (
                <ProblemCard key={link.id} link={link} />
              ))}
            </Rail>
            <Rail
              title="Candidates"
              count={record.solutions.length}
              note={candidateNote}
              empty="No candidate solutions linked yet."
            >
              {record.solutions.map((link) => (
                <CandidateCard key={link.id} link={link} />
              ))}
            </Rail>
          </div>
        </>
      )}
    </Screen>
  );
}
