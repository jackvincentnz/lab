import {
  type AddItemInput,
  addItemInput,
  type DeleteItemInput,
  deleteItemInput,
  type GetItemInput,
  getItemInput,
  type Item,
  itemStatus,
  type ListItemsInput,
  listItemsInput,
  type MoveItemInput,
  moveItemInput,
  type RestoreItemInput,
  restoreItemInput,
  type UpdateItemInput,
  updateItemInput,
} from "./item.js";
import { type ItemStore } from "./store.js";

export type Store = Pick<
  ItemStore,
  "list" | "add" | "update" | "move" | "delete" | "restore"
>;

export const operations = {
  listItems: {
    input: listItemsInput,
    run: (store: Store, input: ListItemsInput = {}) => store.list(input),
  },
  addItem: {
    input: addItemInput,
    run: (store: Store, { name, description, sources }: AddItemInput) =>
      store.add(name, description, sources),
  },
  updateItem: {
    input: updateItemInput,
    run: (store: Store, input: UpdateItemInput) => store.update(input),
  },
  moveItem: {
    input: moveItemInput,
    run: (store: Store, input: MoveItemInput) => store.move(input),
  },
  deleteItem: {
    input: deleteItemInput,
    run: (store: Store, { id }: DeleteItemInput) => store.delete(id),
  },
  restoreItem: {
    input: restoreItemInput,
    run: (store: Store, { id }: RestoreItemInput) => store.restore(id),
  },
  getItem: {
    input: getItemInput,
    run: (store: Store, { item }: GetItemInput) =>
      findItem(store, linkedItemId(item)),
  },
};

export interface FoundItem extends Item {
  deleted: boolean;
}

function linkedItemId(reference: string): string {
  return URL.parse(reference)?.searchParams.get("item") ?? reference;
}

function findItem(store: Store, id: string): FoundItem | undefined {
  const everyItem = { statuses: [...itemStatus.options], doneTodayOnly: false };
  const kept = store.list(everyItem).find((item) => item.id === id);
  if (kept !== undefined) {
    return { ...kept, deleted: false };
  }
  const deleted = store
    .list({ ...everyItem, deleted: true })
    .find((item) => item.id === id);
  return deleted && { ...deleted, deleted: true };
}

export type OperationName = keyof typeof operations;

export const webOnly = [] as const satisfies readonly OperationName[];

export const agentOnly = [
  "getItem",
] as const satisfies readonly OperationName[];

export type WebOperationName = Exclude<
  OperationName,
  (typeof agentOnly)[number]
>;

export type AgentOperationName = Exclude<
  OperationName,
  (typeof webOnly)[number]
>;
