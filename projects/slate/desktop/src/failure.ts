export function lockHeldFailure(
  packaged: boolean,
  dataDir: string,
): Error | undefined {
  // A checkout's build often loses the lock to another worktree's, whose window comes forward
  // unnoticed or is not on screen at all.
  if (packaged) {
    return undefined;
  }
  return new Error(
    `Another Slate, for example one started from another worktree, is running with the data folder ${dataDir}. It has no window if it was started without a terminal. Quit it or stop its process, then start this one again.`,
  );
}

export function failureDialog(error: Error, logPath?: string): string {
  return logPath
    ? `${error.message}\n\nThe log at ${logPath} has details.`
    : error.message;
}
