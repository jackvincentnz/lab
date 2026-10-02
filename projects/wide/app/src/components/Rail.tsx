import type { ReactNode } from "react";

export function Rail({
  title,
  count: n,
  note,
  empty,
  children,
}: {
  title: string;
  count: number;
  note?: string;
  empty: ReactNode;
  children: ReactNode;
}) {
  return (
    <section className="rail">
      <div className="rail-head">
        <h2>{title}</h2>
        <span>{n}</span>
      </div>
      {n === 0 ? <div className="rail-empty">{empty}</div> : children}
      {note && n > 0 && <p className="rail-note">{note}</p>}
    </section>
  );
}
