/** What Slate knows about how it was started. */
interface Launch {
  packaged: boolean;
  stdoutIsTerminal: boolean;
}

/** What a second launch tells the running Slate. */
export interface SecondInstanceData {
  showWindow: boolean;
}

/**
 * Reads whether this launch shows Slate's window, which on macOS also brings Slate in front of
 * other apps. Agents and tests start a checkout's build without a terminal, and its window would
 * otherwise take the keystrokes of someone working in another app.
 */
export function readShowWindow(
  env: NodeJS.ProcessEnv,
  { packaged, stdoutIsTerminal }: Launch,
): boolean {
  return packaged || stdoutIsTerminal || env["SLATE_SHOW_WINDOW"] === "1";
}

/** Reads whether a second launch asks the running Slate to show its window. */
export function secondInstanceShowsWindow(additionalData: unknown): boolean {
  return (additionalData as SecondInstanceData | null)?.showWindow === true;
}
