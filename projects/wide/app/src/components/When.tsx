import { formatDate, relative } from "../lib/format";

export function When({
  value,
  verb,
}: {
  value?: string | null;
  verb?: string;
}) {
  const text = relative(value);
  return (
    <time dateTime={value || undefined} title={formatDate(value)}>
      {verb ? `${verb} ${text}` : text.charAt(0).toUpperCase() + text.slice(1)}
    </time>
  );
}
