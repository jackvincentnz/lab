import { MutationCache, QueryClient } from "@tanstack/react-query";
import { createTRPCClient, httpLink, type TRPCClient } from "@trpc/client";
import { createTRPCContext } from "@trpc/tanstack-react-query";
import type { DayRouter } from "../../service/src/router.js";
import type {
  AddItemInput,
  ItemStatus,
  UpdateItemInput,
} from "../../service/src/schemas.js";
import type { Item } from "../../service/src/store.js";

export type { AddItemInput, Item, ItemStatus, UpdateItemInput };

export type DayClient = TRPCClient<DayRouter>;

export const { TRPCProvider, useTRPC } = createTRPCContext<DayRouter>();

export function createDayClient(url: string): DayClient {
  return createTRPCClient<DayRouter>({ links: [httpLink({ url })] });
}

export function createQueryClient(): QueryClient {
  const queryClient: QueryClient = new QueryClient({
    mutationCache: new MutationCache({
      // The service decides the order, so a change reloads its list rather than patching ours.
      onSuccess: () => queryClient.invalidateQueries(),
    }),
  });
  return queryClient;
}
