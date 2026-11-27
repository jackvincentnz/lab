const DAY_MS = 24 * 60 * 60 * 1000;

export function daysSince(time: Date, now: Date): number {
  return Math.floor((now.getTime() - time.getTime()) / DAY_MS);
}

export function dayName(time: Date, now: Date): string {
  const daysAgo = Math.round((startOfDay(now) - startOfDay(time)) / DAY_MS);
  if (daysAgo === 0) {
    return "Today";
  }
  if (daysAgo === 1) {
    return "Yesterday";
  }
  return time.toLocaleDateString(undefined, {
    weekday: "long",
    month: "long",
    day: "numeric",
    ...(time.getFullYear() !== now.getFullYear() && { year: "numeric" }),
  });
}

function startOfDay(time: Date): number {
  return new Date(
    time.getFullYear(),
    time.getMonth(),
    time.getDate(),
  ).getTime();
}
