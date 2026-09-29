import type { FieldSpec } from "../lib/fields";
import { Prose } from "./Prose";

export function Field({
  title,
  value,
  hint,
}: {
  title: string;
  value?: string | null;
  hint: string;
}) {
  const recorded = Boolean(value?.trim());
  return (
    <section className="field">
      <div className="field-head">
        <h2 className="label">{title}</h2>
        {!recorded && <span className="gap-tag">Not yet recorded</span>}
      </div>
      {recorded ? (
        <Prose>{value as string}</Prose>
      ) : (
        <p className="gap">The guide suggests: {hint}</p>
      )}
    </section>
  );
}

export function Fields<T>({
  spec,
  record,
}: {
  spec: FieldSpec<T>[];
  record: T;
}) {
  return (
    <>
      {spec.map((field) => (
        <Field
          key={String(field.key)}
          title={field.title}
          value={record[field.key] as string | null | undefined}
          hint={field.hint}
        />
      ))}
    </>
  );
}
