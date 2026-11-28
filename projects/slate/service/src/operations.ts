import {
  type AddItemInput,
  addItemInput,
  type DeleteItemInput,
  deleteItemInput,
  type RelateItemsInput,
  relateItemsInput,
  type ListRelatedItemsInput,
  listRelatedItemsInput,
  type ListItemChangesInput,
  listItemChangesInput,
  type ListItemsInput,
  listItemsInput,
  type MoveItemInput,
  moveItemInput,
  type RestoreItemInput,
  restoreItemInput,
  type UnrelateItemsInput,
  unrelateItemsInput,
  type UpdateItemInput,
  updateItemInput,
} from "./item.js";
import { type ItemStore } from "./store.js";

export type Store = Pick<
  ItemStore,
  | "list"
  | "add"
  | "update"
  | "move"
  | "delete"
  | "restore"
  | "changes"
  | "relate"
  | "unrelate"
  | "related"
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
  listItemChanges: {
    input: listItemChangesInput,
    run: (store: Store, { id }: ListItemChangesInput) => store.changes(id),
  },
  relateItems: {
    input: relateItemsInput,
    run: (store: Store, input: RelateItemsInput) => store.relate(input),
  },
  unrelateItems: {
    input: unrelateItemsInput,
    run: (store: Store, input: UnrelateItemsInput) => store.unrelate(input),
  },
  listRelatedItems: {
    input: listRelatedItemsInput,
    run: (store: Store, { id }: ListRelatedItemsInput) => store.related(id),
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
