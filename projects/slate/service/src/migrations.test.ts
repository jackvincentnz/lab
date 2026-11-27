import { randomUUID } from "node:crypto";
import {
  cpSync,
  mkdirSync,
  mkdtempSync,
  readdirSync,
  rmSync,
  writeFileSync,
} from "node:fs";
import { tmpdir } from "node:os";
import { join } from "node:path";
import { DatabaseSync } from "node:sqlite";
import { drizzle } from "drizzle-orm/node-sqlite";
import { migrate } from "drizzle-orm/node-sqlite/migrator";
import { generateKeyBetween } from "fractional-indexing";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { type Item } from "./item.js";
import { itemStatuses } from "./schema.js";
import { openItemStore } from "./store.js";

const migrations = [
  "20261124181218_item",
  "20261126150944_item_deleted",
  "20261126165159_item_done_at",
  "20261126180533_item_description",
  "20261127022610_item_set_aside_statuses",
  "20261127163149_item_source",
  "20261127181136_item_in_progress_status",
  "20261127195930_item_sources",
];

const now = new Date(2026, 10, 26, 12);

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

function migrateUpTo(file: DatabaseSync, last: string) {
  const migrationsFolder = join(dataDir, "migrations");
  for (const name of migrations.slice(0, migrations.indexOf(last) + 1)) {
    cpSync(
      join(import.meta.dirname, "../migrations", name),
      join(migrationsFolder, name),
      { recursive: true },
    );
  }
  migrate(drizzle({ client: file }), { migrationsFolder });
}

/** Builds before the in-progress migration stored the in_progress status as doing. */
function storedStatusBeforeInProgress(status: Item["status"]) {
  return status === "in_progress" ? "doing" : status;
}

function insertItems(
  file: DatabaseSync,
  items: Item[],
  storedStatus: (
    status: Item["status"],
  ) => string = storedStatusBeforeInProgress,
) {
  const insert = file.prepare(
    "INSERT INTO item (id, name, status, rank, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
  );
  for (const item of items) {
    insert.run(
      item.id,
      item.name,
      storedStatus(item.status),
      item.rank,
      item.createdAt,
      item.updatedAt,
    );
  }
}

function randomItems(count: number): Item[] {
  const items: Item[] = [];
  for (let index = 0; index < count; index++) {
    const time = new Date(now.getTime() - index * 60_000).toISOString();
    const status = (["todo", "in_progress", "done"] as const)[index % 3];
    items.push({
      id: randomUUID(),
      name: `Item ${randomUUID()}`,
      description: "",
      sources: [],
      status,
      rank: generateKeyBetween(items.at(-1)?.rank ?? null, null),
      createdAt: time,
      updatedAt: time,
      doneAt: status === "done" ? time : null,
      deleted: false,
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

  it("gets an item table that accepts every status", () => {
    const store = openItemStore(dataDir);
    const items = itemStatuses.map((status) => {
      const added = store.add(`Item ${randomUUID()}`, "", []);
      return store.update({ id: added.id, status, updatedAt: added.updatedAt });
    });

    expect(
      itemStatuses.flatMap((status) => store.list({ statuses: [status] })),
    ).toEqual(items);
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
    const added = openItemStore(dataDir).add(`Item ${randomUUID()}`, "", []);

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
  create(file: DatabaseSync): Item[];
}

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
      insertItems(file, items);
      return items;
    },
  },
  {
    name: "a database from before items could be deleted",
    create(file) {
      migrateUpTo(file, "20261124181218_item");
      const items = randomItems(3);
      insertItems(file, items);
      return items;
    },
  },
  {
    name: "a database from before done items were hidden",
    create(file) {
      migrateUpTo(file, "20261126150944_item_deleted");
      const items = randomItems(3);
      insertItems(file, items);
      return items;
    },
  },
  {
    name: "a database from before items had descriptions",
    create(file) {
      migrateUpTo(file, "20261126165159_item_done_at");
      const items = randomItems(3);
      insertItems(file, items);
      file.exec("UPDATE item SET done_at = updated_at WHERE status = 'done'");
      return items;
    },
  },
  {
    name: "a database from before items could be deferred, delegated or not done",
    create(file) {
      migrateUpTo(file, "20261126180533_item_description");
      const items = randomItems(3).map((item) => ({
        ...item,
        description: `Notes ${randomUUID()}`,
      }));
      const insert = file.prepare(
        "INSERT INTO item (id, name, description, status, rank, created_at, updated_at, done_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
      );
      for (const item of items) {
        insert.run(
          item.id,
          item.name,
          item.description,
          storedStatusBeforeInProgress(item.status),
          item.rank,
          item.createdAt,
          item.updatedAt,
          item.doneAt,
        );
      }
      return items;
    },
  },
  {
    name: "a database from before items had sources",
    create(file) {
      migrateUpTo(file, "20261127022610_item_set_aside_statuses");
      const items = randomItems(3);
      insertItems(file, items);
      file.exec("UPDATE item SET done_at = updated_at WHERE status = 'done'");
      return items;
    },
  },
  {
    name: "a database from before items were in progress",
    create(file) {
      migrateUpTo(file, "20261127163149_item_source");
      const items = randomItems(3);
      insertItems(file, items);
      file.exec("UPDATE item SET done_at = updated_at WHERE status = 'done'");
      return items;
    },
  },
  {
    name: "a database from before items had several sources",
    create(file) {
      migrateUpTo(file, "20261127181136_item_in_progress_status");
      const items = randomItems(3);
      insertItems(file, items, (status) => status);
      file.exec("UPDATE item SET done_at = updated_at WHERE status = 'done'");
      return items;
    },
  },
];

describe.each(earlierSchemas)("$name", ({ create }) => {
  it("keeps its items", () => {
    const items = withFile(create);

    const migrated = openItemStore(dataDir, () => now).list();

    expect(migrated).toEqual(items);
  });

  it("records every migration", () => {
    withFile(create);

    openItemStore(dataDir);

    expect(appliedMigrationNames()).toEqual(migrations);
  });
});

describe("upgrading a database with done items", () => {
  it("hides a done item last changed before today", () => {
    const [todo, , done] = randomItems(3);
    const yesterday = new Date(2026, 10, 25, 23, 30).toISOString();
    withFile((file) => {
      migrateUpTo(file, "20261126150944_item_deleted");
      insertItems(file, [todo, { ...done, updatedAt: yesterday }]);
    });

    const migrated = openItemStore(dataDir, () => now).list();

    expect(migrated).toEqual([todo]);
  });
});

describe("upgrading a database with doing items", () => {
  function rows() {
    return withFile((file) =>
      file.prepare("SELECT * FROM item ORDER BY rank").all(),
    );
  }

  it("marks a doing item in progress and keeps the rest of it", () => {
    const time = now.toISOString();
    withFile((file) => {
      migrateUpTo(file, "20261127163149_item_source");
      file
        .prepare(
          "INSERT INTO item (id, name, description, source, status, rank, created_at, updated_at, deleted, done_at) VALUES (?, ?, ?, ?, 'doing', ?, ?, ?, ?, ?)",
        )
        .run(
          randomUUID(),
          `Item ${randomUUID()}`,
          `Notes ${randomUUID()}`,
          JSON.stringify({
            kind: "link",
            url: `https://example.com/${randomUUID()}`,
            label: `Link ${randomUUID()}`,
          }),
          generateKeyBetween(null, null),
          new Date(now.getTime() - 60_000).toISOString(),
          time,
          1,
          time,
        );
    });

    const [before] = rows();

    openItemStore(dataDir, () => now);

    expect(rows()).toEqual([
      { ...before, source: undefined, sources: "[]", status: "in_progress" },
    ]);
  });
});

describe("upgrading a database with sources", () => {
  it("leaves every item with no sources", () => {
    const [sourced] = randomItems(1);
    withFile((file) => {
      migrateUpTo(file, "20261127181136_item_in_progress_status");
      insertItems(file, [sourced!], (status) => status);
      file.prepare("UPDATE item SET source = ? WHERE id = ?").run(
        JSON.stringify({
          kind: "reference",
          text: `notes/${randomUUID()}.md`,
          label: `Notes ${randomUUID()}`,
        }),
        sourced!.id,
      );
    });

    const migrated = openItemStore(dataDir, () => now).list();

    expect(migrated).toEqual([{ ...sourced, sources: [] }]);
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
      `A newer build of Slate, or a build from a branch that is not merged yet, has migrated this database with ${newer}, so this build cannot open it. Update Slate, or see https://github.com/jackvincentnz/lab/blob/main/projects/slate/docs/migrations.md#a-database-from-another-build.`,
    );
  });
});

describe("backing up before migrating", () => {
  const backups = () => join(dataDir, "backups");

  function backupNames() {
    return readdirSync(backups());
  }

  function namesIn(file: string) {
    const backup = new DatabaseSync(file, { readOnly: true });
    try {
      return backup
        .prepare("SELECT name FROM item ORDER BY rank")
        .all()
        .map(({ name }) => name);
    } finally {
      backup.close();
    }
  }

  function createDatabaseBefore(next: string): Item[] {
    const items = randomItems(3);
    withFile((file) => {
      migrateUpTo(file, migrations[migrations.indexOf(next) - 1]);
      insertItems(file, items);
    });
    return items;
  }

  it("copies the database, named after the first migration it applies", () => {
    const items = createDatabaseBefore("20261126165159_item_done_at");

    openItemStore(dataDir, undefined, { backUp: true });

    expect(backupNames()).toEqual([
      "slate.before-20261126165159_item_done_at.sqlite",
    ]);
    expect(
      namesIn(
        join(backups(), "slate.before-20261126165159_item_done_at.sqlite"),
      ),
    ).toEqual(items.map(({ name }) => name));
  });

  it("includes changes that are still in the write-ahead log", () => {
    withFile((file) => migrateUpTo(file, "20261126150944_item_deleted"));
    const writer = new DatabaseSync(join(dataDir, "slate.sqlite"));
    writer.exec("PRAGMA journal_mode = WAL");
    const items = randomItems(3);
    insertItems(writer, items);

    try {
      openItemStore(dataDir, undefined, { backUp: true });
    } finally {
      writer.close();
    }

    expect(
      namesIn(
        join(backups(), "slate.before-20261126165159_item_done_at.sqlite"),
      ),
    ).toEqual(items.map(({ name }) => name));
  });

  it("keeps the last three copies", () => {
    mkdirSync(backups());
    for (const name of [
      "20201231000000_a",
      "20211231000000_b",
      "20221231000000_c",
    ]) {
      writeFileSync(join(backups(), `slate.before-${name}.sqlite`), "");
    }
    createDatabaseBefore("20261126180533_item_description");

    openItemStore(dataDir, undefined, { backUp: true });

    expect(backupNames()).toEqual([
      "slate.before-20211231000000_b.sqlite",
      "slate.before-20221231000000_c.sqlite",
      "slate.before-20261126180533_item_description.sqlite",
    ]);
  });

  it("copies nothing from a current database", () => {
    openItemStore(dataDir);

    openItemStore(dataDir, undefined, { backUp: true });

    expect(readdirSync(dataDir)).not.toContain("backups");
  });

  it("copies nothing from a new database", () => {
    openItemStore(dataDir, undefined, { backUp: true });

    expect(readdirSync(dataDir)).not.toContain("backups");
  });

  it("copies nothing unless asked", () => {
    createDatabaseBefore("20261126180533_item_description");

    openItemStore(dataDir);

    expect(readdirSync(dataDir)).not.toContain("backups");
  });
});
