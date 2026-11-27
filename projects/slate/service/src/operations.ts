import {
  type AddItemInput,
  addItemInput,
  type DeleteItemInput,
  deleteItemInput,
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
    run: (store: Store, { status }: ListItemsInput = {}) => store.list(status),
  },
  addItem: {
    input: addItemInput,
    run: (store: Store, { name, description }: AddItemInput) =>
      store.add(name, description),
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
};

export type OperationName = keyof typeof operations;

export const webOnly = [] as const satisfies readonly OperationName[];

export const agentOnly = [] as const satisfies readonly OperationName[];

export type WebOperationName = Exclude<
  OperationName,
  (typeof agentOnly)[number]
>;

export type AgentOperationName = Exclude<
  OperationName,
  (typeof webOnly)[number]
>;
