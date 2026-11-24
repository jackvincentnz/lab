import { randomUUID } from "node:crypto";
import { mkdirSync } from "node:fs";
import { join } from "node:path";
import { DatabaseSync } from "node:sqlite";
import { generateKeyBetween } from "fractional-indexing";
import {
  type ItemStatus,
  type MoveItemInput,
  type UpdateItemInput,
} from "./schemas.js";

export interface Item {
  id: string;
  name: string;
  status: ItemStatus;
  rank: string;
  createdAt: string;
  updatedAt: string;
}

// SQLite compares text byte by byte, the same order fractional-indexing's keys sort in.
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

const columns =
  "id, name, status, rank, created_at AS createdAt, updated_at AS updatedAt";

export function openItemStore(
  dataDir: string,
  now: () => Date = () => new Date(),
): ItemStore {
  mkdirSync(dataDir, { recursive: true });
  const database = new DatabaseSync(join(dataDir, "slate.sqlite"));
  database.exec(schema);
  return new ItemStore(database, now);
}

export class ItemStore {
  readonly #database: DatabaseSync;
  readonly #now: () => Date;

  constructor(database: DatabaseSync, now: () => Date) {
    this.#database = database;
    this.#now = now;
  }

  list(): Item[] {
    return this.#database
      .prepare(`SELECT ${columns} FROM item ORDER BY rank`)
      .all() as unknown as Item[];
  }

  /** Adds an item at the bottom, so that new work never displaces what matters most. */
  add(name: string): Item {
    const last = this.#database
      .prepare("SELECT max(rank) AS rank FROM item")
      .get() as { rank: string | null };
    const now = this.#now().toISOString();
    const item: Item = {
      id: randomUUID(),
      name,
      status: "todo",
      rank: generateKeyBetween(last.rank, null),
      createdAt: now,
      updatedAt: now,
    };

    this.#database
      .prepare(
        "INSERT INTO item (id, name, status, rank, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
      )
      .run(
        item.id,
        item.name,
        item.status,
        item.rank,
        item.createdAt,
        item.updatedAt,
      );
    return item;
  }

  update({ id, name, status }: UpdateItemInput): Item | undefined {
    const item = this.#find(id);
    if (!item) {
      return undefined;
    }

    return this.#save({
      ...item,
      name: name ?? item.name,
      status: status ?? item.status,
    });
  }

  move({ id, neighbourId, position }: MoveItemInput): Item | undefined {
    const item = this.#find(id);
    const neighbour = this.#find(neighbourId);
    if (!item || !neighbour) {
      return undefined;
    }
    if (item.id === neighbour.id) {
      // An item is already next to itself.
      return item;
    }

    const rank =
      position === "before"
        ? generateKeyBetween(
            this.#rankAround("max", "<", neighbour.rank, id),
            neighbour.rank,
          )
        : generateKeyBetween(
            neighbour.rank,
            this.#rankAround("min", ">", neighbour.rank, id),
          );
    return this.#save({ ...item, rank });
  }

  #find(id: string): Item | undefined {
    return this.#database
      .prepare(`SELECT ${columns} FROM item WHERE id = ?`)
      .get(id) as Item | undefined;
  }

  #rankAround(
    closest: "max" | "min",
    side: "<" | ">",
    rank: string,
    excludedId: string,
  ): string | null {
    const row = this.#database
      .prepare(
        `SELECT ${closest}(rank) AS rank FROM item WHERE rank ${side} ? AND id <> ?`,
      )
      .get(rank, excludedId) as { rank: string | null };
    return row.rank;
  }

  #save(item: Item): Item {
    const saved = { ...item, updatedAt: this.#now().toISOString() };

    this.#database
      .prepare(
        "UPDATE item SET name = ?, status = ?, rank = ?, updated_at = ? WHERE id = ?",
      )
      .run(saved.name, saved.status, saved.rank, saved.updatedAt, saved.id);
    return saved;
  }
}
