import { randomUUID } from "node:crypto";
import { mkdirSync } from "node:fs";
import { join } from "node:path";
import { DatabaseSync } from "node:sqlite";
import { and, asc, desc, eq, gt, lt, ne } from "drizzle-orm";
import { drizzle, type NodeSQLiteDatabase } from "drizzle-orm/node-sqlite";
import { generateKeyBetween } from "fractional-indexing";
import { item } from "./schema.js";
import { type MoveItemInput, type UpdateItemInput } from "./schemas.js";

export type Item = typeof item.$inferSelect;

const schema = `
  CREATE TABLE IF NOT EXISTS item (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('todo', 'doing', 'done')),
    rank TEXT NOT NULL UNIQUE,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
  ) STRICT
`;

export function openItemStore(
  dataDir: string,
  now: () => Date = () => new Date(),
): ItemStore {
  mkdirSync(dataDir, { recursive: true });
  const client = new DatabaseSync(join(dataDir, "slate.sqlite"));
  client.exec(schema);
  return new ItemStore(drizzle({ client }), now);
}

export class ItemStore {
  readonly #database: NodeSQLiteDatabase;
  readonly #now: () => Date;

  constructor(database: NodeSQLiteDatabase, now: () => Date) {
    this.#database = database;
    this.#now = now;
  }

  list(): Item[] {
    return this.#database.select().from(item).orderBy(item.rank).all();
  }

  /** Adds an item at the bottom, so that new work never displaces what matters most. */
  add(name: string): Item {
    const last = this.#database
      .select({ rank: item.rank })
      .from(item)
      .orderBy(desc(item.rank))
      .get();
    const now = this.#now().toISOString();
    const added: Item = {
      id: randomUUID(),
      name,
      status: "todo",
      rank: generateKeyBetween(last?.rank ?? null, null),
      createdAt: now,
      updatedAt: now,
    };

    this.#database.insert(item).values(added).run();
    return added;
  }

  update({ id, name, status }: UpdateItemInput): Item | undefined {
    const found = this.#find(id);
    if (!found) {
      return undefined;
    }

    return this.#save({
      ...found,
      name: name ?? found.name,
      status: status ?? found.status,
    });
  }

  move({ id, neighbourId, position }: MoveItemInput): Item | undefined {
    const moving = this.#find(id);
    const neighbour = this.#find(neighbourId);
    if (!moving || !neighbour) {
      return undefined;
    }
    if (moving.id === neighbour.id) {
      // An item is already next to itself.
      return moving;
    }

    const next = this.#rankBeside(neighbour.rank, position, id);
    const rank =
      position === "before"
        ? generateKeyBetween(next, neighbour.rank)
        : generateKeyBetween(neighbour.rank, next);
    return this.#save({ ...moving, rank });
  }

  #find(id: string): Item | undefined {
    return this.#database.select().from(item).where(eq(item.id, id)).get();
  }

  /** The closest rank on the given side of `rank`, ignoring the item being moved. */
  #rankBeside(
    rank: string,
    side: MoveItemInput["position"],
    movingId: string,
  ): string | null {
    const closest = this.#database
      .select({ rank: item.rank })
      .from(item)
      .where(
        and(
          side === "before" ? lt(item.rank, rank) : gt(item.rank, rank),
          ne(item.id, movingId),
        ),
      )
      .orderBy(side === "before" ? desc(item.rank) : asc(item.rank))
      .get();
    return closest?.rank ?? null;
  }

  #save(changed: Item): Item {
    const saved = { ...changed, updatedAt: this.#now().toISOString() };

    this.#database
      .update(item)
      .set({
        name: saved.name,
        status: saved.status,
        rank: saved.rank,
        updatedAt: saved.updatedAt,
      })
      .where(eq(item.id, saved.id))
      .run();
    return saved;
  }
}
