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

export function failureDialog(error: Error, logPath?: string): string {
  return logPath
    ? `${error.message}\n\nThe log at ${logPath} has details.`
    : error.message;
}
