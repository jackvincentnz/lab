export function formatDate(value?: string | null) {
  return value
    ? new Date(value).toLocaleString(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
      })
    : "Not recorded";
}

export function relative(value?: string | null) {
  if (!value) return "not recorded";
  const then = new Date(value);
  const now = new Date();
  const day = (d: Date) =>
    new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();
  const days = Math.round((day(now) - day(then)) / 86_400_000);
  const time = then.toLocaleTimeString(undefined, { timeStyle: "short" });
  if (days === 0) return `today, ${time}`;
  if (days === 1) return `yesterday, ${time}`;
  if (days > 1 && days < 7) return `${days} days ago`;
  return then.toLocaleDateString(undefined, { dateStyle: "medium" });
}

export function count(n: number, one: string, many = `${one}s`) {
  return `${n} ${n === 1 ? one : many}`;
}
