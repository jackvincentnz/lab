import { getFragmentData, type FragmentType } from "../__generated__";
import {
  DecisionFieldsFragmentDoc,
  QuestionFieldsFragmentDoc,
  type QuestionFieldsFragment,
} from "../__generated__/graphql";
import { count } from "../lib/format";
import { CopyId } from "./CopyId";
import { Prose } from "./Prose";
import { When } from "./When";

export interface RefinementRecord {
  questions: FragmentType<typeof QuestionFieldsFragmentDoc>[];
  decisions: FragmentType<typeof DecisionFieldsFragmentDoc>[];
}

function QuestionItem({ question: q }: { question: QuestionFieldsFragment }) {
  return (
    <article className="refinement-item">
      <h3>{q.question}</h3>
      {q.context && <Prose>{q.context}</Prose>}
      {q.answer && (
        <div className="refinement-answer">
          <span className="label">
            {q.status === "ANSWERED"
              ? "Answer"
              : "Earlier answer · question reopened"}
          </span>
          <Prose>{q.answer}</Prose>
        </div>
      )}
      {q.source && <div className="card-meta">Source: {q.source}</div>}
      <div className="card-meta">
        <When verb="Updated" value={q.updatedAt} /> · <CopyId id={q.id} />
      </div>
    </article>
  );
}

export function RefinementSection({ record }: { record: RefinementRecord }) {
  const questions = getFragmentData(
    QuestionFieldsFragmentDoc,
    record.questions,
  );
  const decisions = getFragmentData(
    DecisionFieldsFragmentDoc,
    record.decisions,
  );
  const open = questions.filter((q) => q.status === "OPEN");
  const answered = questions.filter((q) => q.status === "ANSWERED");
  return (
    <>
      <section className="field">
        <h2 className="label">Decisions</h2>
        <p className="field-hint">
          Recorded human positions and when to reconsider them. These do not
          establish delivery readiness.
        </p>
        {decisions.length === 0 ? (
          <p className="dim">No decisions recorded yet.</p>
        ) : (
          decisions.map((d) => (
            <article className="refinement-item" key={d.id}>
              <h3>{d.decision}</h3>
              {d.rationale && <Prose>{d.rationale}</Prose>}
              {d.revisitWhen && (
                <div className="refinement-answer">
                  <span className="label">Reconsider when</span>
                  <Prose>{d.revisitWhen}</Prose>
                </div>
              )}
              {d.source && <div className="card-meta">Source: {d.source}</div>}
              <div className="card-meta">
                <When verb="Updated" value={d.updatedAt} /> ·{" "}
                <CopyId id={d.id} />
              </div>
            </article>
          ))
        )}
      </section>
      <section className="field">
        <h2 className="label">Questions</h2>
        {open.length === 0 ? (
          <p className="dim">No open questions recorded.</p>
        ) : (
          <>
            <p className="card-meta">{count(open.length, "open question")}</p>
            {open.map((q) => (
              <QuestionItem key={q.id} question={q} />
            ))}
          </>
        )}
        {answered.length > 0 && (
          <details className="answered-questions">
            <summary>{count(answered.length, "answered question")}</summary>
            {answered.map((q) => (
              <QuestionItem key={q.id} question={q} />
            ))}
          </details>
        )}
      </section>
    </>
  );
}
