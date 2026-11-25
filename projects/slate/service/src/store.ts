import { randomUUID } from "node:crypto";
import { mkdirSync } from "node:fs";
import { join } from "node:path";
import { DatabaseSync } from "node:sqlite";
import {
  and,
  asc,
  desc,
  eq,
  getTableName,
  gt,
  lt,
  ne,
  notInArray,
} from "drizzle-orm";
import { readMigrationFiles } from "drizzle-orm/migrator";
import { drizzle, type NodeSQLiteDatabase } from "drizzle-orm/node-sqlite";
import { migrate } from "drizzle-orm/node-sqlite/migrator";
import { sqliteTable, text } from "drizzle-orm/sqlite-core";
import { generateKeyBetween } from "fractional-indexing";
import { type Item, type MoveItemInput, type UpdateItemInput } from "./item.js";
import { item } from "./schema.js";

const migrationsFolder = join(import.meta.dirname, "../migrations");

const sqliteSchema = sqliteTable("sqlite_schema", { name: text() });

// Drizzle's migrator records each migration it applies here by name.
const appliedMigration = sqliteTable("__drizzle_migrations", { name: text() });

export function openItemStore(
  dataDir: string,
  now: () => Date = () => new Date(),
): ItemStore {
  mkdirSync(dataDir, { recursive: true });
  return openDatabase(new DatabaseSync(join(dataDir, "slate.sqlite")), now);
}

/** Opens a store that keeps its items in memory, so that tests can run the real service without files. */
export function openInMemoryItemStore(
  now: () => Date = () => new Date(),
): ItemStore {
  return openDatabase(new DatabaseSync(":memory:"), now);
}

function openDatabase(client: DatabaseSync, now: () => Date): ItemStore {
  const database = drizzle({ client });

  refuseNewerDatabase(database);
  migrate(database, { migrationsFolder });
  return new ItemStore(database, now);
}

/** Stops an older build from opening a database that a newer build has migrated, whose schema it does not know. */
function refuseNewerDatabase(database: NodeSQLiteDatabase) {
  const tracked = database
    .select()
    .from(sqliteSchema)
    .where(eq(sqliteSchema.name, getTableName(appliedMigration)))
    .get();
  if (!tracked) {
    return;
  }

  const known = readMigrationFiles({ migrationsFolder }).map(
    (migration) => migration.name,
  );
  const unknown = database
    .select()
    .from(appliedMigration)
    .where(notInArray(appliedMigration.name, known))
    .all();
  if (unknown.length > 0) {
    throw new Error(
      `A newer build of Slate has migrated this database with ${unknown.map(({ name }) => name).join(", ")}, so this build cannot open it. Update Slate.`,
    );
  }
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

  update({ id, ...changes }: UpdateItemInput): Item | undefined {
    const found = this.#find(id);
    if (!found) {
      return undefined;
    }

    return this.#save({ ...found, ...changes });
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
    const { id, ...fields } = saved;

    this.#database.update(item).set(fields).where(eq(item.id, id)).run();
    return saved;
  }
}
