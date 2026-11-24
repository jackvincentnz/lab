/** The message the service sends Electron main once it is listening. */
export interface ServiceReady {
  url: string;
}

interface ParentPort {
  postMessage(message: ServiceReady): void;
}

/** Tells Electron main the service's URL, when Electron started the service as a utility process. */
export function announceReady(url: string): void {
  const { parentPort } = process as NodeJS.Process & {
    parentPort?: ParentPort;
  };
  parentPort?.postMessage({ url });
}
