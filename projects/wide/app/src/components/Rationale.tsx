import { Prose } from "./Prose";

export function Rationale({
  children,
  contribution = false,
}: {
  children: string;
  contribution?: boolean;
}) {
  return (
    <div className="why">
      <span className="label">
        {contribution ? "Expected contribution" : "Why linked"}
      </span>
      <Prose>{children}</Prose>
    </div>
  );
}
