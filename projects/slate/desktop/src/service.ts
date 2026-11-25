import { type ServiceStartup } from "../../service/src/ready.js";

/** The events of Electron's `UtilityProcess` that the service's start-up uses. */
export interface ServiceProcess {
  once(event: "message", listener: (message: ServiceStartup) => void): unknown;
  once(event: "exit", listener: (code: number) => void): unknown;
}

/**
 * Resolves with the service's URL once it is listening, or rejects with the reason it gives, or
 * if it exits first.
 */
export function waitUntilListening(service: ServiceProcess): Promise<string> {
  return new Promise((resolve, reject) => {
    service.once("message", (message) =>
      "url" in message
        ? resolve(message.url)
        : reject(new Error(message.error)),
    );
    service.once("exit", (code) =>
      reject(
        new Error(
          `The Slate service exited with code ${code} before it was listening`,
        ),
      ),
    );
  });
}
