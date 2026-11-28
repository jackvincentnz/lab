import { sql } from "drizzle-orm";
import {
  check,
  index,
  integer,
  primaryKey,
  sqliteTable,
  text,
} from "drizzle-orm/sqlite-core";
import type { ItemSource } from "./item.js";

export const itemStatuses = [
  "todo",
  "in_progress",
  "done",
  "deferred",
  "wont_do",
  "delegated",
] as const;

export const item = sqliteTable(
  "item",
  {
    id: text().primaryKey(),
    name: text().notNull(),
    description: text().notNull().default(""),
    sources: text({ mode: "json" })
      .$type<ItemSource[]>()
      .notNull()
      .default(sql`'[]'`),
    status: text({ enum: itemStatuses }).notNull(),
    // SQLite compares text byte by byte, the same order fractional-indexing's keys sort in.
    rank: text().notNull().unique(),
    createdAt: text("created_at").notNull(),
    updatedAt: text("updated_at").notNull(),
    deleted: integer({ mode: "boolean" }).notNull().default(false),
    doneAt: text("done_at"),
  },
  (table) => [
    check(
      "item_status",
      sql`${table.status} IN (${sql.raw(itemStatuses.map((status) => `'${status}'`).join(", "))})`,
    ),
  ],
);

export const actors = ["user", "agent"] as const;

export const itemChangeKinds = [
  "added",
  "name",
  "description",
  "sources",
  "status",
  "rank",
  "deleted",
  "restored",
] as const;

function oneOf(column: unknown, values: readonly string[]) {
  return sql`${column} IN (${sql.raw(values.map((value) => `'${value}'`).join(", "))})`;
}

export const itemChange = sqliteTable(
  "item_change",
  {
    id: integer().primaryKey({ autoIncrement: true }),
    itemId: text("item_id")
      .notNull()
      .references(() => item.id),
    at: text().notNull(),
    actor: text({ enum: actors }).notNull(),
    kind: text({ enum: itemChangeKinds }).notNull(),
    status: text({ enum: itemStatuses }),
  },
  (table) => [
    index("item_change_item_id").on(table.itemId),
    check("item_change_actor", oneOf(table.actor, actors)),
    check("item_change_kind", oneOf(table.kind, itemChangeKinds)),
    check("item_change_status_value", oneOf(table.status, itemStatuses)),
    check(
      "item_change_status",
      sql`(${table.kind} = 'status') = (${table.status} IS NOT NULL)`,
    ),
  ],
);

export const itemLink = sqliteTable(
  "item_link",
  {
    itemId: text("item_id")
      .notNull()
      .references(() => item.id),
    linkedId: text("linked_id")
      .notNull()
      .references(() => item.id),
  },
  (table) => [
    primaryKey({ columns: [table.itemId, table.linkedId] }),
    index("item_link_linked_id").on(table.linkedId),
    check("item_link_order", sql`${table.itemId} < ${table.linkedId}`),
  ],
);
