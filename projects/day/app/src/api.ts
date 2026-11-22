import { createContext, useContext } from "react";
import { createTRPCClient, httpLink } from "@trpc/client";
import type { DayRouter } from "../../service/src/router.js";
import type { ItemStatus, UpdateItemInput } from "../../service/src/schemas.js";
import type { Item } from "../../service/src/store.js";

export type { Item, ItemStatus, UpdateItemInput };

export interface DayApi {
  listItems(): Promise<Item[]>;
  addItem(name: string): Promise<Item>;
  updateItem(change: UpdateItemInput): Promise<Item>;
}

export function createDayApi(url: string): DayApi {
  const client = createTRPCClient<DayRouter>({ links: [httpLink({ url })] });
  return {
    listItems: () => client.listItems.query(),
    addItem: (name) => client.addItem.mutate({ name }),
    updateItem: (change) => client.updateItem.mutate(change),
  };
}

export const DayApiContext = createContext<DayApi>(createDayApi("/api"));

export function useDayApi(): DayApi {
  return useContext(DayApiContext);
}
