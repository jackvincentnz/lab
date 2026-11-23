import { z } from "zod";

export const itemStatus = z.enum(["todo", "doing", "done"]);

export type ItemStatus = z.infer<typeof itemStatus>;

const itemId = z.uuid();

const itemName = z.string().trim().min(1, "Give a name that is not blank");

export const addItemInput = z.object({
  name: itemName.describe("What the work is, in a few words."),
});

export type AddItemInput = z.infer<typeof addItemInput>;

export const updateItemInput = z
  .object({
    id: itemId.describe("The id of the item to change."),
    name: itemName.optional().describe("The item's new name."),
    status: itemStatus.optional().describe("The item's new status."),
  })
  .refine((input) => input.name !== undefined || input.status !== undefined, {
    message: "Give a name or a status to change",
  });

export type UpdateItemInput = z.infer<typeof updateItemInput>;

export const moveItemInput = z.object({
  id: itemId.describe("The id of the item to move."),
  neighbourId: itemId.describe("The id of the item to place it next to."),
  position: z
    .enum(["before", "after"])
    .describe(
      "Place it before the neighbour, so that it matters more, or after, so that it matters less.",
    ),
});

export type MoveItemInput = z.infer<typeof moveItemInput>;
