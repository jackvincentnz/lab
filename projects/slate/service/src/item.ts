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
  label: sourceLabel("link").describe("A few words that say what the link is."),
});

const referenceSource = z.object({
  kind: z.literal("reference"),
  text: z
    .string()
    .trim()
    .min(1, "Give a reference that is not blank")
    .max(1000, "Give a reference of at most 1000 characters")
    .refine((text) => !/[\r\n]/.test(text), "Give a reference on one line")
    .describe(
      "Where the item came from, on one line, kept as given: a file path, with a line or heading if it helps, a claude:// link to a Claude Desktop session, or anything else.",
    ),
  label: sourceLabel("reference").describe(
    "A few words that say what the reference is.",
  ),
});

export const newSource = z.discriminatedUnion("kind", [
  linkSource,
  referenceSource,
]);

export type NewSource = z.infer<typeof newSource>;

export const sourceId = z.uuid();

export const itemSource = z.discriminatedUnion("kind", [
  linkSource.extend({ id: sourceId }),
  referenceSource.extend({ id: sourceId }),
]);

export type ItemSource = z.infer<typeof itemSource>;

/** What no two of an item's sources share: a link's url or a reference's text. */
export function sourceKey(source: NewSource): string {
  return source.kind === "link" ? source.url : source.text;
}

export function withoutRepeats<Source extends NewSource>(
  sources: Source[],
): Source[] {
  const seen = new Set<string>();
  return sources.filter((source) => {
    const key = sourceKey(source);
    if (seen.has(key)) {
      return false;
    }
    seen.add(key);
    return true;
  });
}

/** The desktop app opens only https links in the browser. */
function isSourceLink(url: string): boolean {
  return URL.parse(url)?.protocol === "https:";
}

function sourceLabel(kind: string) {
  return z.string().trim().min(1, `Give the ${kind} a label that is not blank`);
}

export const itemSchema = createSelectSchema(item, {
  id: z.uuid(),
  name: (name) => name.trim().min(1, "Give a name that is not blank"),
  description: (description) =>
    description.overwrite((value) => value.trimEnd()),
  sources: z.array(itemSource),
}).omit({ deleted: true });

export type Item = z.infer<typeof itemSchema>;

export const itemStatus = itemSchema.shape.status;

export type ItemStatus = z.infer<typeof itemStatus>;

const { id, name, description } = itemSchema.shape;

const sourceKinds =
  'A source is a link, {"kind": "link", "url", "label"}, whose url starts with https://, or a reference, {"kind": "reference", "text", "label"}, for anything without an https link, such as a file path or a claude:// link, kept as given. The user can copy a reference, and open a claude:// one in Claude Desktop. For example a pull request: {"kind": "link", "url": "https://example.com/pull/12", "label": "Pull request"}. A line in a notes file: {"kind": "reference", "text": "~/notes/TODO.md:12", "label": "TODO list"}. A heading in a daily note: {"kind": "reference", "text": "~/notes/daily/2026-11-27.md#Follow-ups", "label": "Daily note"}. The Claude Desktop session that captured the item: {"kind": "reference", "text": "claude://claude.ai/epitaxy/local_0b6e4c1a-5d2f-4e8a-9c7b-3f1d2a6e8b40", "label": "Claude Desktop session"}. A session in the Code tab of the Claude desktop app finds its own link as link in the result of its get_session tool called with "self".';

const sourceKeyInput = z
  .string()
  .trim()
  .min(1, "Give a link's url or a reference's text that is not blank");

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
  source: sourceKeyInput
    .optional()
    .describe(
      "Only the items with a source whose link url or reference text is exactly this, such as a pull request link, to check whether it is already tracked. Combine it with the other inputs, for example statuses of every status and doneTodayOnly false to look across every item that is not deleted.",
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
  sources: z
    .array(newSource)
    .default([])
    .describe(
      `Where the item came from, such as the Claude Desktop session, notes file, documents or pull requests that it came out of. Give the main one first, without ids; each gets one. A source repeating an earlier one's url or text is left out. ${sourceKinds}`,
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
    addSources: z
      .array(newSource)
      .optional()
      .describe(
        `Sources to add after the item's others, in this order, without ids; each gets one. A source with the same url or text as one the item has is left out. ${sourceKinds}`,
      ),
    removeSources: z
      .array(sourceId)
      .optional()
      .describe(
        "The ids of the sources to remove, from the item's sources in list_items or the last tool that returned the item. Refused if the item has no source with one of them.",
      ),
    replaceSource: z
      .object({
        id: sourceId.describe("The id of the source to replace."),
        to: newSource.describe(
          "The source to put in its place, without an id; it keeps the id it replaces.",
        ),
      })
      .optional()
      .describe(
        "Changes one source in place, keeping its id and position, such as to correct a link or relabel it. Refused if the item has no source with that id, or has another source with the url or text of to.",
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
      input.addSources !== undefined ||
      input.removeSources !== undefined ||
      input.replaceSource !== undefined ||
      input.status !== undefined,
    {
      message:
        "Give a name, a description, sources to add, remove or replace, or a status to change",
    },
  );

export type UpdateItemInput = z.infer<typeof updateItemInput>;

export class ItemChangedError extends Error {
  constructor(id: string) {
    super(`The item ${id} has changed since it was read`);
  }
}

export class SourceChangeError extends Error {}

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
