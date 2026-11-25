import type { PropsWithChildren } from "react";
import {
  createTRPCClient,
  localLink,
  TRPCClientError,
  type TRPCLink,
} from "@trpc/client";
import { observable, type Unsubscribable } from "@trpc/server/observable";
import type { WebOperationName } from "../../../service/src/operations.js";
import { slateRouter, type SlateRouter } from "../../../service/src/router.js";
import {
  type ItemStore,
  openInMemoryItemStore,
} from "../../../service/src/store.js";
import { createQueryClient } from "../api";
import { SlateProvider } from "../SlateProvider";

/** The real service over an in-memory list, with calls that a test can fail or hold. */
export class TestSlate {
  readonly store: ItemStore = openInMemoryItemStore();
  readonly #calls = new Map<WebOperationName, number>();
  readonly #failures = new Map<WebOperationName, number>();
  readonly #held = new Set<WebOperationName>();

  /** Fails the next `times` calls to the operation before they reach the service, as a lost connection would. */
  fail(operation: WebOperationName, times = Number.POSITIVE_INFINITY) {
    this.#failures.set(operation, times);
  }

  /** Holds calls to the operation before they reach the service, so that the UI stays waiting for them. */
  hold(operation: WebOperationName) {
    this.#held.add(operation);
  }

  /** How many calls to the operation the UI has sent, including failed and held ones. */
  calls(operation: WebOperationName): number {
    return this.#calls.get(operation) ?? 0;
  }

  async arrive(operation: WebOperationName): Promise<void> {
    this.#calls.set(operation, this.calls(operation) + 1);
    const failures = this.#failures.get(operation) ?? 0;
    if (failures > 0) {
      this.#failures.set(operation, failures - 1);
      throw new Error("offline");
    }
    if (this.#held.has(operation)) {
      await new Promise<never>(() => undefined);
    }
  }
}

function interceptLink(slate: TestSlate): TRPCLink<SlateRouter> {
  return () =>
    ({ op, next }) =>
      observable((observer) => {
        let call: Unsubscribable | undefined;
        let cancelled = false;
        slate.arrive(op.path as WebOperationName).then(
          () => {
            if (!cancelled) {
              call = next(op).subscribe(observer);
            }
          },
          (error: Error) => observer.error(TRPCClientError.from(error)),
        );
        return () => {
          cancelled = true;
          call?.unsubscribe();
        };
      });
}

/** Wraps the UI in a client that calls the router in this process, so tests run the real client, cache and service. */
export function withSlate(slate: TestSlate) {
  const client = createTRPCClient<SlateRouter>({
    links: [
      interceptLink(slate),
      localLink({
        router: slateRouter,
        createContext: () => Promise.resolve({ store: slate.store }),
      }),
    ],
  });
  const queryClient = createQueryClient();
  const defaults = queryClient.getDefaultOptions();
  queryClient.setDefaultOptions({
    ...defaults,
    queries: { ...defaults.queries, retry: false },
  });

  return function SlateWrapper({ children }: PropsWithChildren) {
    return (
      <SlateProvider client={client} queryClient={queryClient}>
        {children}
      </SlateProvider>
    );
  };
}
