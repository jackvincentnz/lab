import type { FieldSpec } from "../lib/fields";
import { Prose } from "./Prose";

interface FieldProps {
  title: string;
  value?: string | null;
  hint: string;
}

export function Field({ title, value, hint }: FieldProps) {
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

interface FieldsProps<T> {
  spec: FieldSpec<T>[];
  record: T;
}

export function Fields<T>({ spec, record }: FieldsProps<T>) {
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
