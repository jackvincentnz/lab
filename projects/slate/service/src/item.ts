import { z } from "zod";
import { createSelectSchema } from "drizzle-orm/zod";
import { item } from "./schema.js";

export const itemSchema = createSelectSchema(item, {
  id: z.uuid(),
  name: (name) => name.trim().min(1, "Give a name that is not blank"),
}).omit({ deleted: true, doneAt: true });

export type Item = z.infer<typeof itemSchema>;

export const itemStatus = itemSchema.shape.status;

export type ItemStatus = z.infer<typeof itemStatus>;

const { id, name, updatedAt } = itemSchema.shape;

export const addItemInput = z.object({
  name: name.describe("What the work is, in a few words."),
});

export type AddItemInput = z.infer<typeof addItemInput>;

export const updateItemInput = z
  .object({
    id: id.describe("The id of the item to change."),
    name: name.optional().describe("The item's new name."),
    status: itemStatus.optional().describe("The item's new status."),
    updatedAt: updatedAt
      .optional()
      .describe(
        "The item's updatedAt as list_items returned it. If the item has changed since, the update is refused, so that a change you have not seen is not overwritten.",
      ),
  })
  .refine((input) => input.name !== undefined || input.status !== undefined, {
    message: "Give a name or a status to change",
  });

export type UpdateItemInput = z.infer<typeof updateItemInput>;

export class ItemChangedError extends Error {
  constructor(id: string) {
    super(`The item ${id} has changed since it was read`);
  }
}

export const moveItemInput = z.object({
  id: id.describe("The id of the item to move."),
  neighbourId: id.describe("The id of the item to place it next to."),
  position: z
    .enum(["before", "after"])
    .describe(
      "Place it before the neighbour, so that it matters more, or after, so that it matters less.",
    ),
});

export type MoveItemInput = z.infer<typeof moveItemInput>;

export const deleteItemInput = z.object({
  id: id.describe("The id of the item to delete."),
});

export type DeleteItemInput = z.infer<typeof deleteItemInput>;

export const restoreItemInput = z.object({
  id: id.describe("The id of the deleted item to put back."),
});

export type RestoreItemInput = z.infer<typeof restoreItemInput>;
