import { sql } from "drizzle-orm";
import { check, sqliteTable, text } from "drizzle-orm/sqlite-core";

export const itemStatuses = ["todo", "doing", "done"] as const;

export const item = sqliteTable(
  "item",
  {
    id: text().primaryKey(),
    name: text().notNull(),
    status: text({ enum: itemStatuses }).notNull(),
    // SQLite compares text byte by byte, the same order fractional-indexing's keys sort in.
    rank: text().notNull().unique(),
    createdAt: text("created_at").notNull(),
    updatedAt: text("updated_at").notNull(),
  },
  (table) => [
    check("item_status", sql`${table.status} IN ('todo', 'doing', 'done')`),
  ],
);
