import { z } from "zod";
import { createSelectSchema } from "drizzle-orm/zod";
import { item } from "./schema.js";

const linkSource = z.object({
  kind: z.literal("link"),
  url: z
    .string()
    .trim()
    .refine(isSourceLink, "Give a link that starts with https://")
    .describe("Where the item came from, starting with https://."),
  label: z
    .string()
    .trim()
    .min(1, "Give the link a label that is not blank")
    .describe("A few words that say what the link is."),
});

export const itemSource = z.discriminatedUnion("kind", [linkSource]);

export type ItemSource = z.infer<typeof itemSource>;

/** The desktop app opens only https links in the browser. */
function isSourceLink(url: string): boolean {
  return URL.parse(url)?.protocol === "https:";
}

export const itemSchema = createSelectSchema(item, {
  id: z.uuid(),
  name: (name) => name.trim().min(1, "Give a name that is not blank"),
  description: (description) =>
    description.overwrite((value) => value.trimEnd()),
  source: itemSource.nullable(),
}).omit({ deleted: true });

export type Item = z.infer<typeof itemSchema>;

export const itemStatus = itemSchema.shape.status;

export type ItemStatus = z.infer<typeof itemStatus>;

const { id, name, description } = itemSchema.shape;

const sourceExample =
  'For example {"kind": "link", "url": "https://claude.ai/chat/…", "label": "Claude Desktop session"} for the session that captured the item.';

export const listItemsInput = z.object({
  statuses: z
    .array(itemStatus)
    .min(1)
    .default(["todo", "in_progress", "done"])
    .describe("Only the items with one of these statuses. Give at least one."),
  doneTodayOnly: z
    .boolean()
    .default(true)
    .describe(
      "When true, leaves out done items marked done before midnight at the start of today, in the time zone of the user's machine. Give false to include done items from every day.",
    ),
  deleted: z
    .boolean()
    .default(false)
    .describe(
      "When true, lists only deleted items; when false, none. A deleted item that was done on an earlier day is listed only with doneTodayOnly false.",
    ),
  orderBy: z
    .enum(["rank", "doneAt", "updatedAt"])
    .default("rank")
    .describe(
      "rank is priority order, the most important first. doneAt and updatedAt are newest first, and items without that time come last in priority order. A deleted item's updatedAt is when it was deleted.",
    ),
});

export type ListItemsInput = z.input<typeof listItemsInput>;

export type ListQuery = z.output<typeof listItemsInput>;

export const addItemInput = z.object({
  name: name.describe("What the work is, in a few words."),
  description: description
    .default("")
    .describe(
      "Context that belongs with the item, such as notes, links or why it matters. Markdown.",
    ),
  source: itemSource
    .optional()
    .describe(
      `Where the item came from, as a link with a label, such as the Claude Desktop session, document or pull request that it came out of. ${sourceExample}`,
    ),
});

export type AddItemInput = z.infer<typeof addItemInput>;

export const updateItemInput = z
  .object({
    id: id.describe("The id of the item to change."),
    name: name.optional().describe("The item's new name."),
    description: description
      .optional()
      .describe(
        "The item's new description, which replaces the old one. Markdown. Give an empty one to remove it.",
      ),
    source: itemSource
      .nullable()
      .optional()
      .describe(
        `The item's new source, which replaces the old one. Give null to remove it. ${sourceExample}`,
      ),
    status: itemStatus.optional().describe("The item's new status."),
    updatedAt: z
      .string({
        error:
          "Give the item's updatedAt from list_items or the last tool that returned the item",
      })
      .describe(
        "Required. The item's latest updatedAt, from list_items or the last tool that returned the item. If the item has changed since, the update is refused, so that a change you have not seen is not overwritten.",
      ),
  })
  .refine(
    (input) =>
      input.name !== undefined ||
      input.description !== undefined ||
      input.source !== undefined ||
      input.status !== undefined,
    { message: "Give a name, a description, a source or a status to change" },
  );

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
