import {
  MutationCache,
  QueryClient,
  type QueryKey,
} from "@tanstack/react-query";
import { createTRPCClient, httpLink, type TRPCClient } from "@trpc/client";
import { createTRPCContext } from "@trpc/tanstack-react-query";
import type { SlateRouter } from "../../service/src/router.js";
import type {
  AddItemInput,
  Item,
  ItemStatus,
  MoveItemInput,
  UpdateItemInput,
} from "../../service/src/item.js";

export type { AddItemInput, Item, ItemStatus, MoveItemInput, UpdateItemInput };

export type SlateClient = TRPCClient<SlateRouter>;

export const { TRPCProvider, useTRPC } = createTRPCContext<SlateRouter>();

export function createSlateClient(url: string): SlateClient {
  return createTRPCClient<SlateRouter>({ links: [httpLink({ url })] });
}

export function createQueryClient(): QueryClient {
  const queryClient: QueryClient = new QueryClient({
    // The service runs on this machine, so the browser going offline must not pause requests.
    defaultOptions: {
      queries: { networkMode: "always" },
      mutations: { networkMode: "always" },
    },
    mutationCache: new MutationCache({
      // Agents change the list too, so even a failed change reloads what the service has.
      onSettled: (_data, _error, change) =>
        queryClient.invalidateQueries({
          predicate: (query) => changeAffectsQuery(change, query.queryKey),
        }),
    }),
  });
  return queryClient;
}

export function changeAffectsQuery(
  change: unknown,
  queryKey: QueryKey,
): boolean {
  const [, queryOptions] = queryKey;
  const queriedItemId = itemIdOf(inputOf(queryOptions));
  const queryIsForOneItem = queriedItemId !== undefined;
  return !queryIsForOneItem || queriedItemId === itemIdOf(change);
}

function inputOf(queryOptions: unknown): unknown {
  return typeof queryOptions === "object" &&
    queryOptions !== null &&
    "input" in queryOptions
    ? queryOptions.input
    : undefined;
}

function itemIdOf(value: unknown): unknown {
  return typeof value === "object" && value !== null && "id" in value
    ? value.id
    : undefined;
}
