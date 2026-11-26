import { type ServiceStartup } from "../../service/src/ready.js";

export interface ServiceProcess {
  once(event: "message", listener: (message: ServiceStartup) => void): unknown;
  once(event: "exit", listener: (code: number) => void): unknown;
}

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
