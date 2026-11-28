import {
  MutationCache,
  QueryClient,
  type QueryKey,
} from "@tanstack/react-query";
import {
  createTRPCClient,
  httpLink,
  httpSubscriptionLink,
  retryLink,
  splitLink,
  type TRPCClient,
  type TRPCClientErrorLike,
} from "@trpc/client";
import { createTRPCContext } from "@trpc/tanstack-react-query";
import type { SlateRouter } from "../../service/src/router.js";
import type {
  AddItemInput,
  DeleteItemInput,
  Item,
  ItemChange,
  ItemSource,
  NewSource,
  ItemStatus,
  RelateItemsInput,
  UnrelateItemsInput,
  ListItemChangesInput,
  ListRelatedItemsInput,
  ListItemsInput,
  MoveItemInput,
  RestoreItemInput,
  UpdateItemInput,
} from "../../service/src/item.js";

export type {
  AddItemInput,
  DeleteItemInput,
  Item,
  ItemChange,
  ItemSource,
  NewSource,
  ItemStatus,
  RelateItemsInput,
  UnrelateItemsInput,
  ListItemChangesInput,
  ListRelatedItemsInput,
  ListItemsInput,
  MoveItemInput,
  RestoreItemInput,
  UpdateItemInput,
};

const RECONNECT_MS = 3000;

export type SlateClient = TRPCClient<SlateRouter>;

export const { TRPCProvider, useTRPC } = createTRPCContext<SlateRouter>();

export function createSlateClient(url: string): SlateClient {
  return createTRPCClient<SlateRouter>({
    links: [
      splitLink({
        condition: (op) => op.type === "subscription",
        true: [
          // EventSource stops for good on an error response, such as the dev server's proxy answering 502 while the service restarts.
          retryLink({ retry: () => true, retryDelayMs: () => RECONNECT_MS }),
          httpSubscriptionLink({ url }),
        ],
        false: httpLink({ url }),
      }),
    ],
  });
}

export function isChangedElsewhere(
  error: TRPCClientErrorLike<SlateRouter> | null,
): boolean {
  return error?.data?.code === "CONFLICT";
}

export function createQueryClient(): QueryClient {
  const queryClient: QueryClient = new QueryClient({
    // The service runs on this machine, so the browser going offline must not pause requests.
    defaultOptions: {
      // ListUpdates reloads when the page becomes visible or reconnects instead, so that the reload waits for a drag or edit to end.
      queries: {
        networkMode: "always",
        refetchOnWindowFocus: false,
        refetchOnReconnect: false,
      },
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
