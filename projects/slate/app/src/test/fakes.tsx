import type { PropsWithChildren } from "react";
import { createTRPCClient, TRPCClientError, type TRPCLink } from "@trpc/client";
import { TRPCError } from "@trpc/server";
import { observable } from "@trpc/server/observable";
import { TRPC_ERROR_CODES_BY_KEY } from "@trpc/server/rpc";
import { vi, type Mock } from "@lab/test-utils";
import type { SlateRouter } from "../../../service/src/router.js";
import {
  createQueryClient,
  type AddItemInput,
  type DeleteItemInput,
  type Item,
  type MoveItemInput,
  type RestoreItemInput,
  type UpdateItemInput,
} from "../api";
import { SlateProvider } from "../SlateProvider";

export interface FakeSlateApi {
  listItems: Mock<() => Promise<Item[]>>;
  addItem: Mock<(input: AddItemInput) => Promise<Item>>;
  updateItem: Mock<(input: UpdateItemInput) => Promise<Item>>;
  moveItem: Mock<(input: MoveItemInput) => Promise<Item>>;
  deleteItem: Mock<(input: DeleteItemInput) => Promise<Item>>;
  restoreItem: Mock<(input: RestoreItemInput) => Promise<Item>>;
}

export function fakeSlateApi(items: Item[] = []): FakeSlateApi {
  return {
    listItems: vi.fn<FakeSlateApi["listItems"]>().mockResolvedValue(items),
    addItem: vi.fn<FakeSlateApi["addItem"]>(),
    updateItem: vi.fn<FakeSlateApi["updateItem"]>(),
    moveItem: vi.fn<FakeSlateApi["moveItem"]>(),
    deleteItem: vi.fn<FakeSlateApi["deleteItem"]>(),
    restoreItem: vi.fn<FakeSlateApi["restoreItem"]>(),
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
          (error: unknown) => observer.error(clientError(error)),
        );
      });
}

// The service sends a TRPCError's code to the client in its error data.
function clientError(error: unknown) {
  if (error instanceof TRPCError) {
    return TRPCClientError.from({
      error: {
        code: TRPC_ERROR_CODES_BY_KEY[error.code],
        message: error.message,
        data: { code: error.code },
      },
    });
  }
  return TRPCClientError.from(error as Error);
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
    description: "",
    source: null,
    status: "todo",
    rank: `a${itemCount}`,
    createdAt: "2026-11-22T09:00:00.000Z",
    updatedAt: "2026-11-22T09:00:00.000Z",
    doneAt: null,
    ...fields,
  };
}
