import type { PropsWithChildren } from "react";
import { createTRPCClient, TRPCClientError, type TRPCLink } from "@trpc/client";
import { observable } from "@trpc/server/observable";
import { vi, type Mock } from "@lab/test-utils";
import type { SlateRouter } from "../../../service/src/router.js";
import {
  createQueryClient,
  type AddItemInput,
  type Item,
  type MoveItemInput,
  type UpdateItemInput,
} from "../api";
import { SlateProvider } from "../SlateProvider";

export interface FakeSlateApi {
  listItems: Mock<() => Promise<Item[]>>;
  addItem: Mock<(input: AddItemInput) => Promise<Item>>;
  updateItem: Mock<(input: UpdateItemInput) => Promise<Item>>;
  moveItem: Mock<(input: MoveItemInput) => Promise<Item>>;
}

export function fakeSlateApi(items: Item[] = []): FakeSlateApi {
  return {
    listItems: vi.fn<FakeSlateApi["listItems"]>().mockResolvedValue(items),
    addItem: vi.fn<FakeSlateApi["addItem"]>(),
    updateItem: vi.fn<FakeSlateApi["updateItem"]>(),
    moveItem: vi.fn<FakeSlateApi["moveItem"]>(),
  };
}

// Answers each call from the fake rather than over HTTP, so tests run the real client and cache.
function fakeLink(api: FakeSlateApi): TRPCLink<SlateRouter> {
  return () =>
    ({ op }) =>
      observable((observer) => {
        const procedure = api[op.path as keyof FakeSlateApi] as (
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

export function withSlateApi(api: FakeSlateApi) {
  const client = createTRPCClient<SlateRouter>({ links: [fakeLink(api)] });
  const queryClient = createQueryClient();
  const defaults = queryClient.getDefaultOptions();
  queryClient.setDefaultOptions({
    ...defaults,
    queries: { ...defaults.queries, retry: false },
  });

  return function SlateApiWrapper({ children }: PropsWithChildren) {
    return (
      <SlateProvider client={client} queryClient={queryClient}>
        {children}
      </SlateProvider>
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
