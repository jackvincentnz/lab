interface Launch {
  packaged: boolean;
  stdoutIsTerminal: boolean;
}

export interface SecondInstanceData {
  showWindow: boolean;
}

// On macOS, showing a window brings Slate in front of other apps, so a window that agents or tests
// open would take the keystrokes of someone working in another app.
export function readShowWindow(
  env: NodeJS.ProcessEnv,
  { packaged, stdoutIsTerminal }: Launch,
): boolean {
  return packaged || stdoutIsTerminal || env["SLATE_SHOW_WINDOW"] === "1";
}

export function secondInstanceShowsWindow(additionalData: unknown): boolean {
  return (additionalData as SecondInstanceData | null)?.showWindow === true;
}
