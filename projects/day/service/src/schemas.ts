import { z } from "zod";

export const itemStatus = z.enum(["todo", "doing", "done"]);

export type ItemStatus = z.infer<typeof itemStatus>;

const itemId = z.uuid();

const itemName = z.string().trim().min(1);

export const addItemInput = z.object({ name: itemName });

export type AddItemInput = z.infer<typeof addItemInput>;

export const updateItemInput = z
  .object({
    id: itemId,
    name: itemName.optional(),
    status: itemStatus.optional(),
  })
  .refine((input) => input.name !== undefined || input.status !== undefined, {
    message: "Give a name or a status to change",
  });

export type UpdateItemInput = z.infer<typeof updateItemInput>;

export const moveItemInput = z
  .object({
    id: itemId,
    neighbourId: itemId,
    position: z.enum(["before", "after"]),
  })
  .refine((input) => input.id !== input.neighbourId, {
    message: "An item cannot move next to itself",
    path: ["neighbourId"],
  });

export type MoveItemInput = z.infer<typeof moveItemInput>;
