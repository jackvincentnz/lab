/**
 * Why Slate quits when another instance holds the lock on its data folder, or undefined when that
 * instance brings its window forward, which is all the user expects.
 */
export function lockHeldFailure(
  packaged: boolean,
  dataDir: string,
): Error | undefined {
  // A checkout's build often loses the lock to another build, such as another worktree's, whose
  // window then comes forward unnoticed.
  if (packaged) {
    return undefined;
  }
  return new Error(
    `Another Slate, for example one started from another worktree, is running with the data folder ${dataDir}. Quit it, then start this one again.`,
  );
}

/** The text of the dialog that tells the user why Slate could not start. */
export function failureDialog(error: unknown, logPath?: string): string {
  const reason = error instanceof Error ? error.message : String(error);
  return logPath ? `${reason}\n\nThe log at ${logPath} has details.` : reason;
}
