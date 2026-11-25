import {
  type AddItemInput,
  addItemInput,
  type MoveItemInput,
  moveItemInput,
  type UpdateItemInput,
  updateItemInput,
} from "./schemas.js";
import { type ItemStore } from "./store.js";

export type Store = Pick<ItemStore, "list" | "add" | "update" | "move">;

/** Each operation's input and result, which the tRPC router and the MCP tools both serve. */
export const operations = {
  listItems: {
    run: (store: Store) => store.list(),
  },
  addItem: {
    input: addItemInput,
    run: (store: Store, { name }: AddItemInput) => store.add(name),
  },
  updateItem: {
    input: updateItemInput,
    run: (store: Store, input: UpdateItemInput) => store.update(input),
  },
  moveItem: {
    input: moveItemInput,
    run: (store: Store, input: MoveItemInput) => store.move(input),
  },
};

export type OperationName = keyof typeof operations;
