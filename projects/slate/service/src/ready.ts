export interface ServiceReady {
  url: string;
}

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

export function announceReady(url: string): void {
  parentPort()?.postMessage({ url });
}

export function announceFailure(error: string): void {
  parentPort()?.postMessage({ error });
}
