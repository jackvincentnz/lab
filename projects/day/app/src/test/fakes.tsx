import type { PropsWithChildren } from "react";
import { createTRPCClient, TRPCClientError, type TRPCLink } from "@trpc/client";
import { observable } from "@trpc/server/observable";
import { vi, type Mock } from "@lab/test-utils";
import type { DayRouter } from "../../../service/src/router.js";
import {
  createQueryClient,
  type AddItemInput,
  type Item,
  type UpdateItemInput,
} from "../api";
import { DayProvider } from "../DayProvider";

export interface FakeDayApi {
  listItems: Mock<() => Promise<Item[]>>;
  addItem: Mock<(input: AddItemInput) => Promise<Item>>;
  updateItem: Mock<(input: UpdateItemInput) => Promise<Item>>;
}

export function fakeDayApi(items: Item[] = []): FakeDayApi {
  return {
    listItems: vi.fn<FakeDayApi["listItems"]>().mockResolvedValue(items),
    addItem: vi.fn<FakeDayApi["addItem"]>(),
    updateItem: vi.fn<FakeDayApi["updateItem"]>(),
  };
}

// Answers each call from the fake rather than over HTTP, so tests run the real client and cache.
function fakeLink(api: FakeDayApi): TRPCLink<DayRouter> {
  return () =>
    ({ op }) =>
      observable((observer) => {
        const procedure = api[op.path as keyof FakeDayApi] as (
          input: unknown,
        ) => Promise<unknown>;
        procedure(op.input).then(
          (data) => {
            observer.next({ result: { type: "data", data } });
            observer.complete();
          },
          (error: unknown) =>
            observer.error(TRPCClientError.from(error as Error)),
        );
      });
}

export function withDayApi(api: FakeDayApi) {
  const client = createTRPCClient<DayRouter>({ links: [fakeLink(api)] });
  const queryClient = createQueryClient();
  const defaults = queryClient.getDefaultOptions();
  queryClient.setDefaultOptions({
    ...defaults,
    queries: { ...defaults.queries, retry: false },
  });

  return function DayApiWrapper({ children }: PropsWithChildren) {
    return (
      <DayProvider client={client} queryClient={queryClient}>
        {children}
      </DayProvider>
    );
  };
}

let itemCount = 0;

export function item(fields: Partial<Item> = {}): Item {
  itemCount += 1;
  return {
    id: crypto.randomUUID(),
    name: `Item ${itemCount}`,
    status: "todo",
    rank: `a${itemCount}`,
    createdAt: "2026-11-22T09:00:00.000Z",
    updatedAt: "2026-11-22T09:00:00.000Z",
    ...fields,
  };
}
