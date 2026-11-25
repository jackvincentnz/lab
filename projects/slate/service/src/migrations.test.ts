import { randomUUID } from "node:crypto";
import { mkdtempSync, rmSync } from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { DatabaseSync } from "node:sqlite";
import { generateKeyBetween } from "fractional-indexing";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { type Item } from "./schemas.js";
import { openItemStore } from "./store.js";

const migrations = ["20261124181218_item"];

let dataDir: string;

beforeEach(() => {
  dataDir = mkdtempSync(join(tmpdir(), "slate-migrations-"));
});

afterEach(() => {
  rmSync(dataDir, { recursive: true, force: true });
});

function withFile<T>(use: (file: DatabaseSync) => T): T {
  const file = new DatabaseSync(join(dataDir, "slate.sqlite"));
  try {
    return use(file);
  } finally {
    file.close();
  }
}

function appliedMigrations() {
  return withFile((file) =>
    file.prepare("SELECT id, name, applied_at FROM __drizzle_migrations").all(),
  );
}

function appliedMigrationNames() {
  return appliedMigrations().map(({ name }) => name);
}

function randomItems(count: number): Item[] {
  const items: Item[] = [];
  for (let index = 0; index < count; index++) {
    const time = new Date(Date.now() - index * 60_000).toISOString();
    items.push({
      id: randomUUID(),
      name: `Item ${randomUUID()}`,
      status: (["todo", "doing", "done"] as const)[index % 3],
      rank: generateKeyBetween(items.at(-1)?.rank ?? null, null),
      createdAt: time,
      updatedAt: time,
    });
  }
  return items;
}

describe("a new folder", () => {
  it("gets every migration", () => {
    openItemStore(dataDir);

    expect(appliedMigrationNames()).toEqual(migrations);
  });

  it("gets a strict item table", () => {
    openItemStore(dataDir);

    const [table] = withFile((file) =>
      file.prepare("PRAGMA table_list('item')").all(),
    );
    expect(table).toMatchObject({ strict: 1 });
  });

  it("gets an item table that refuses an unknown status", () => {
    openItemStore(dataDir);
    const [item] = randomItems(1);

    const insert = () =>
      withFile((file) =>
        file
          .prepare(
            "INSERT INTO item (id, name, status, rank, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
          )
          .run(
            item.id,
            item.name,
            "blocked",
            item.rank,
            item.createdAt,
            item.updatedAt,
          ),
      );

    expect(insert).toThrow(/CHECK constraint failed/);
  });
});

describe("a current database", () => {
  it("keeps its items when reopened", () => {
    const added = openItemStore(dataDir).add(`Item ${randomUUID()}`);

    const items = openItemStore(dataDir).list();

    expect(items).toEqual([added]);
  });

  it("records nothing new when reopened", () => {
    openItemStore(dataDir);
    const applied = appliedMigrations();

    openItemStore(dataDir);

    expect(appliedMigrations()).toEqual(applied);
  });
});

interface EarlierSchema {
  name: string;
  /** Builds the database as a build with this schema left it, and returns the items it should keep. */
  create(file: DatabaseSync): Item[];
}

// One case per schema that a released build could have left behind, oldest first.
const earlierSchemas: EarlierSchema[] = [
  {
    name: "a database from before Slate had migrations",
    create(file) {
      file.exec(`
        CREATE TABLE item (
          id TEXT PRIMARY KEY,
          name TEXT NOT NULL,
          status TEXT NOT NULL CHECK (status IN ('todo', 'doing', 'done')),
          rank TEXT NOT NULL UNIQUE,
          created_at TEXT NOT NULL,
          updated_at TEXT NOT NULL
        ) STRICT
      `);
      const items = randomItems(3);
      const insert = file.prepare(
        "INSERT INTO item (id, name, status, rank, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
      );
      for (const item of items) {
        insert.run(
          item.id,
          item.name,
          item.status,
          item.rank,
          item.createdAt,
          item.updatedAt,
        );
      }
      return items;
    },
  },
];

describe.each(earlierSchemas)("$name", ({ create }) => {
  it("keeps its items", () => {
    const items = withFile(create);

    const migrated = openItemStore(dataDir).list();

    expect(migrated).toEqual(items);
  });

  it("records every migration", () => {
    withFile(create);

    openItemStore(dataDir);

    expect(appliedMigrationNames()).toEqual(migrations);
  });
});

describe("a database from a newer build", () => {
  it("is refused", () => {
    openItemStore(dataDir);
    const newer = `99991231235959_${randomUUID()}`;
    withFile((file) =>
      file
        .prepare(
          "INSERT INTO __drizzle_migrations (hash, created_at, name) VALUES (?, ?, ?)",
        )
        .run(randomUUID(), Date.now(), newer),
    );

    const open = () => openItemStore(dataDir);

    expect(open).toThrow(
      `A newer build of Slate has migrated this database with ${newer}, so this build cannot open it. Update Slate.`,
    );
  });
});
