/** The message the service sends Electron main once it is listening. */
export interface ServiceReady {
  url: string;
}

/** The message the service sends Electron main when it cannot start. */
export interface ServiceFailed {
  error: string;
}

export type ServiceStartup = ServiceReady | ServiceFailed;

interface ParentPort {
  postMessage(message: ServiceStartup): void;
}

function parentPort(): ParentPort | undefined {
  return (process as NodeJS.Process & { parentPort?: ParentPort }).parentPort;
}

/** Tells Electron main the service's URL, when Electron started the service as a utility process. */
export function announceReady(url: string): void {
  parentPort()?.postMessage({ url });
}

/** Tells Electron main why the service cannot start, when Electron started it as a utility process. */
export function announceFailure(error: string): void {
  parentPort()?.postMessage({ error });
}
