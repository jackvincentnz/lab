import { on } from "node:events";
import { type AnyProcedure, initTRPC, TRPCError } from "@trpc/server";
import { type ListChanges } from "./changes.js";
import { type Item, ItemChangedError, SourceNotFoundError } from "./item.js";
import { operations, type Store, type WebOperationName } from "./operations.js";

export interface Context {
  store: Store;
  changes: ListChanges;
}

export const keepAlive = { pingMs: 5_000, reconnectAfterInactivityMs: 15_000 };

// After the machine sleeps, a connection can stay open with neither end noticing that it is dead, so the page reconnects when the pings stop.
const t = initTRPC.context<Context>().create({
  sse: {
    ping: { enabled: true, intervalMs: keepAlive.pingMs },
    client: {
      reconnectAfterInactivityMs: keepAlive.reconnectAfterInactivityMs,
    },
  },
});

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
    try {
      while (!(await changes.next()).done) {
        yield null;
      }
    } finally {
      await changes.return?.();
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
    if (error instanceof SourceNotFoundError) {
      throw new TRPCError({
        code: "BAD_REQUEST",
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
