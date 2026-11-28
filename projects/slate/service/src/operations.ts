import {
  type AddItemInput,
  addItemInput,
  type DeleteItemInput,
  deleteItemInput,
  type LinkItemsInput,
  linkItemsInput,
  type ListLinkedItemsInput,
  listLinkedItemsInput,
  type ListItemChangesInput,
  listItemChangesInput,
  type ListItemsInput,
  listItemsInput,
  type MoveItemInput,
  moveItemInput,
  type RestoreItemInput,
  restoreItemInput,
  type UnlinkItemsInput,
  unlinkItemsInput,
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
  | "link"
  | "unlink"
  | "linked"
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
  linkItems: {
    input: linkItemsInput,
    run: (store: Store, input: LinkItemsInput) => store.link(input),
  },
  unlinkItems: {
    input: unlinkItemsInput,
    run: (store: Store, input: UnlinkItemsInput) => store.unlink(input),
  },
  listLinkedItems: {
    input: listLinkedItemsInput,
    run: (store: Store, { id }: ListLinkedItemsInput) => store.linked(id),
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
