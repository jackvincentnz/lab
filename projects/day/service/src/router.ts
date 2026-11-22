import { initTRPC, TRPCError } from "@trpc/server";
import { addItemInput, moveItemInput, updateItemInput } from "./schemas.js";
import { type Item, type ItemStore } from "./store.js";

export interface Context {
  store: ItemStore;
}

const t = initTRPC.context<Context>().create();

export const dayRouter = t.router({
  listItems: t.procedure.query(({ ctx }) => ctx.store.list()),
  addItem: t.procedure
    .input(addItemInput)
    .mutation(({ ctx, input }) => ctx.store.add(input.name)),
  updateItem: t.procedure
    .input(updateItemInput)
    .mutation(({ ctx, input }) => found(ctx.store.update(input))),
  moveItem: t.procedure
    .input(moveItemInput)
    .mutation(({ ctx, input }) => found(ctx.store.move(input))),
});

export type DayRouter = typeof dayRouter;

function found(item: Item | undefined): Item {
  if (!item) {
    throw new TRPCError({ code: "NOT_FOUND", message: "No such item" });
  }
  return item;
}
