import { type ServiceReady } from "../../service/src/ready.js";

/** The events of Electron's `UtilityProcess` that the service's start-up uses. */
export interface ServiceProcess {
  once(event: "message", listener: (message: ServiceReady) => void): unknown;
  once(event: "exit", listener: (code: number) => void): unknown;
}

/** Resolves with the service's URL once it is listening, or rejects if it exits first. */
export function waitUntilListening(service: ServiceProcess): Promise<string> {
  return new Promise((resolve, reject) => {
    service.once("message", ({ url }) => resolve(url));
    service.once("exit", (code) =>
      reject(
        new Error(
          `The Slate service exited with code ${code} before it was listening`,
        ),
      ),
    );
  });
}
