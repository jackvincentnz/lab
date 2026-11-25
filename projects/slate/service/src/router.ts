import { type AnyProcedure, initTRPC, TRPCError } from "@trpc/server";
import { type Item } from "./item.js";
import { operations, type Store, type WebOperationName } from "./operations.js";

export interface Context {
  store: Store;
}

const t = initTRPC.context<Context>().create();

const { listItems, addItem, updateItem, moveItem } = operations;

export const slateRouter = t.router({
  listItems: t.procedure.query(({ ctx }) => listItems.run(ctx.store)),
  addItem: t.procedure
    .input(addItem.input)
    .mutation(({ ctx, input }) => addItem.run(ctx.store, input)),
  updateItem: t.procedure
    .input(updateItem.input)
    .mutation(({ ctx, input }) => found(updateItem.run(ctx.store, input))),
  moveItem: t.procedure
    .input(moveItem.input)
    .mutation(({ ctx, input }) => found(moveItem.run(ctx.store, input))),
} satisfies Record<WebOperationName, AnyProcedure>);

export type SlateRouter = typeof slateRouter;

function found(item: Item | undefined): Item {
  if (!item) {
    throw new TRPCError({ code: "NOT_FOUND", message: "No such item" });
  }
  return item;
}
