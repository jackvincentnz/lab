import { formatDate, relative } from "../lib/format";

interface WhenProps {
  value?: string | null;
  verb?: string;
}

export function When({ value, verb }: WhenProps) {
  const text = relative(value);
  return (
    <time dateTime={value || undefined} title={formatDate(value)}>
      {verb ? `${verb} ${text}` : text.charAt(0).toUpperCase() + text.slice(1)}
    </time>
  );
}
