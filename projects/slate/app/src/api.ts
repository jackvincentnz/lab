import { MutationCache, QueryClient } from "@tanstack/react-query";
import { createTRPCClient, httpLink, type TRPCClient } from "@trpc/client";
import { createTRPCContext } from "@trpc/tanstack-react-query";
import type { SlateRouter } from "../../service/src/router.js";
import type {
  AddItemInput,
  ItemStatus,
  MoveItemInput,
  UpdateItemInput,
} from "../../service/src/schemas.js";
import type { Item } from "../../service/src/store.js";

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
      // The service decides the order, so a change reloads its list rather than patching ours.
      onSuccess: () => queryClient.invalidateQueries(),
    }),
  });
  return queryClient;
}
