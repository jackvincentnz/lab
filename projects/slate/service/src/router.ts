import { on } from "node:events";
import { type AnyProcedure, initTRPC, TRPCError } from "@trpc/server";
import { type ListChanges } from "./changes.js";
import { type Item, ItemChangedError } from "./item.js";
import { operations, type Store, type WebOperationName } from "./operations.js";

export interface Context {
  store: Store;
  changes: ListChanges;
}

const t = initTRPC.context<Context>().create();

const { listItems, addItem, updateItem, moveItem, deleteItem, restoreItem } =
  operations;

const operationProcedures = {
  listItems: t.procedure
    .input(listItems.input.optional())
    .query(({ ctx, input }) => listItems.run(ctx.store, input)),
  addItem: t.procedure
    .input(addItem.input)
    .mutation(({ ctx, input }) => addItem.run(ctx.store, input)),
  updateItem: t.procedure
    .input(updateItem.input)
    .mutation(({ ctx, input }) =>
      found(unchanged(() => updateItem.run(ctx.store, input))),
    ),
  moveItem: t.procedure
    .input(moveItem.input)
    .mutation(({ ctx, input }) => found(moveItem.run(ctx.store, input))),
  deleteItem: t.procedure
    .input(deleteItem.input)
    .mutation(({ ctx, input }) => found(deleteItem.run(ctx.store, input))),
  restoreItem: t.procedure
    .input(restoreItem.input)
    .mutation(({ ctx, input }) => found(restoreItem.run(ctx.store, input))),
} satisfies Record<WebOperationName, AnyProcedure>;

export const slateRouter = t.router({
  ...operationProcedures,
  listChanged: t.procedure.subscription(async function* ({ ctx, signal }) {
    const changes = on(ctx.changes, "change", { signal });
    while (!(await changes.next()).done) {
      yield null;
    }
  }),
});

export type SlateRouter = typeof slateRouter;

function unchanged<T>(update: () => T): T {
  try {
    return update();
  } catch (error) {
    if (error instanceof ItemChangedError) {
      throw new TRPCError({
        code: "CONFLICT",
        message: error.message,
        cause: error,
      });
    }
    throw error;
  }
}

function found(item: Item | undefined): Item {
  if (!item) {
    throw new TRPCError({ code: "NOT_FOUND", message: "No such item" });
  }
  return item;
}
