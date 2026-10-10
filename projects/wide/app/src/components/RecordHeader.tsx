import type { ReactNode } from "react";
import { singular, type Kind } from "../lib/kinds";

export function RecordHeader({
  kind,
  title,
  meta,
}: {
  kind: Kind;
  title: string;
  meta: ReactNode[];
}) {
  return (
    <div className="record-head">
      <div className="eyebrow">{singular[kind]}</div>
      <h1>{title}</h1>
      <div className="meta">
        {meta.map((item, i) => (
          <span key={i}>{item}</span>
        ))}
      </div>
    </div>
  );
}
