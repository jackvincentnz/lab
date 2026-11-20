import { Prose } from "./Prose";

interface RationaleProps {
  children: string;
  contribution?: boolean;
}

export function Rationale({ children, contribution = false }: RationaleProps) {
  return (
    <div className="why">
      <span className="label">
        {contribution ? "Expected contribution" : "Why linked"}
      </span>
      <Prose>{children}</Prose>
    </div>
  );
}
