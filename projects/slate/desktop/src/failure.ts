export function lockHeldFailure(
  packaged: boolean,
  dataDir: string,
): Error | undefined {
  // An installed app's other instance brings its window forward, which is all the user expects. A
  // checkout's build often loses the lock to another worktree's, whose window comes forward unnoticed.
  if (packaged) {
    return undefined;
  }
  return new Error(
    `Another Slate, for example one started from another worktree, is running with the data folder ${dataDir}. Quit it, then start this one again.`,
  );
}

export function failureDialog(error: unknown, logPath?: string): string {
  const reason = error instanceof Error ? error.message : String(error);
  return logPath ? `${reason}\n\nThe log at ${logPath} has details.` : reason;
}
