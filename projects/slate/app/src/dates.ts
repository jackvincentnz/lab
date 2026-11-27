const DAY_MS = 24 * 60 * 60 * 1000;

export function daysSince(time: Date, now: Date): number {
  return Math.floor((now.getTime() - time.getTime()) / DAY_MS);
}
