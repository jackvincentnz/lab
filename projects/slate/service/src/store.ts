import { randomUUID } from "node:crypto";
import { mkdirSync, readdirSync, rmSync } from "node:fs";
import { join } from "node:path";
import { DatabaseSync } from "node:sqlite";
import {
  type Column,
  and,
  asc,
  desc,
  eq,
  getTableName,
  gt,
  gte,
  inArray,
  lt,
  ne,
  or,
  sql,
  type SQL,
} from "drizzle-orm";
import { readMigrationFiles } from "drizzle-orm/migrator";
import { drizzle, type NodeSQLiteDatabase } from "drizzle-orm/node-sqlite";
import { migrate } from "drizzle-orm/node-sqlite/migrator";
import { sqliteTable, text } from "drizzle-orm/sqlite-core";
import { generateKeyBetween } from "fractional-indexing";
import {
  type Actor,
  type Item,
  type ItemChange,
  ItemChangedError,
  type ItemSource,
  type ItemStatus,
  type ListItemsInput,
  type NewSource,
  SourceChangeError,
  sourceKey,
  withoutRepeats,
  listItemsInput,
  type RelateItemsInput,
  type UnrelateItemsInput,
  type ListQuery,
  type MoveItemInput,
  type UpdateItemInput,
} from "./item.js";
import { item, itemChange, itemRelation } from "./schema.js";

const itemColumns = {
  id: item.id,
  name: item.name,
  description: item.description,
  sources: item.sources,
  status: item.status,
  rank: item.rank,
  createdAt: item.createdAt,
  updatedAt: item.updatedAt,
  doneAt: item.doneAt,
} satisfies Record<keyof Item, unknown>;

const listed = eq(item.deleted, false);

/** Today starts at midnight local to the service, which runs on the user's machine. */
function notDoneBeforeToday(now: Date) {
  const startOfToday = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate(),
  ).toISOString();
  return or(ne(item.status, "done"), gte(item.doneAt, startOfToday));
}

function hasSource(key: string) {
  return sql`EXISTS (SELECT 1 FROM json_each(${item.sources}) WHERE coalesce(json_extract(value, '$.url'), json_extract(value, '$.text')) = ${key})`;
}

function matchesSearch(search: string) {
  return and(...search.split(/\s+/).map(containsWord));
}

function containsWord(word: string) {
  const pattern = `%${word.replace(/[\\%_]/g, "\\$&")}%`;
  const contains = (text: SQL | Column) =>
    sql`${text} LIKE ${pattern} ESCAPE '\\'`;
  return or(
    contains(item.name),
    contains(item.description),
    sql`EXISTS (SELECT 1 FROM json_each(${item.sources}) WHERE ${contains(sql`json_extract(value, '$.label')`)} OR ${contains(sql`coalesce(json_extract(value, '$.url'), json_extract(value, '$.text'))`)})`,
  );
}

const statusOrder = {
  in_progress: 0,
  todo: 1,
  delegated: 2,
  deferred: 3,
  done: 4,
  wont_do: 5,
} satisfies Record<ItemStatus, number>;

const byStatus = sql`CASE ${item.status} ${sql.join(
  Object.entries(statusOrder).map(
    ([status, order]) => sql`WHEN ${status} THEN ${order}`,
  ),
  sql` `,
)} END`;

const listOrder = {
  rank: [],
  status: [byStatus],
  doneAt: [desc(item.doneAt)],
  updatedAt: [desc(item.updatedAt)],
} satisfies Record<ListQuery["orderBy"], SQL[]>;

const migrationsFolder = join(import.meta.dirname, "../migrations");

const sqliteSchema = sqliteTable("sqlite_schema", { name: text() });

const appliedMigration = sqliteTable("__drizzle_migrations", { name: text() });

const keptBackups = 3;

export function databaseFile(dataDir: string): string {
  return join(dataDir, "slate.sqlite");
}

export interface StoreOptions {
  backUp?: boolean;
}

export function openItemStore(
  dataDir: string,
  now: () => Date = () => new Date(),
  { backUp = false }: StoreOptions = {},
): ItemStore {
  mkdirSync(dataDir, { recursive: true });
  const client = new DatabaseSync(databaseFile(dataDir));
  const database = drizzle({ client });

  const known = readMigrationFiles({ migrationsFolder }).map(
    (migration) => migration.name,
  );
  const applied = appliedMigrations(database);
  refuseNewerDatabase(applied, known);
  const [next] = known.filter((name) => !applied.includes(name));
  if (backUp && next && hasTables(database)) {
    backUpDatabase(client, join(dataDir, "backups"), next);
  }
  migrate(database, { migrationsFolder });
  return new ItemStore(database, now, "user");
}

function hasTables(database: NodeSQLiteDatabase): boolean {
  return database.select().from(sqliteSchema).get() !== undefined;
}

function appliedMigrations(database: NodeSQLiteDatabase): string[] {
  const tracked = database
    .select()
    .from(sqliteSchema)
    .where(eq(sqliteSchema.name, getTableName(appliedMigration)))
    .get();
  if (!tracked) {
    return [];
  }

  return database
    .select()
    .from(appliedMigration)
    .all()
    .map(({ name }) => name ?? "");
}

function refuseNewerDatabase(applied: string[], known: string[]) {
  const unknown = applied.filter((name) => !known.includes(name));
  if (unknown.length > 0) {
    throw new Error(
      `A newer build of Slate, or a build from a branch that is not merged yet, has migrated this database with ${unknown.join(", ")}, so this build cannot open it. Update Slate, or see ${newerDatabaseHelp}.`,
    );
  }
}

const newerDatabaseHelp =
  "https://github.com/jackvincentnz/lab/blob/main/projects/slate/docs/migrations.md#a-database-from-another-build";

/**
 * `VACUUM INTO` copies through SQLite, so the copy includes changes still in the journal or WAL
 * file that copying `slate.sqlite` alone would miss.
 */
function backUpDatabase(client: DatabaseSync, folder: string, next: string) {
  mkdirSync(folder, { recursive: true });
  const backup = join(folder, `slate.before-${next}.sqlite`);
  rmSync(backup, { force: true });
  client.prepare("VACUUM INTO ?").run(backup);

  // Migration names start with their timestamp, so the oldest backups sort first.
  const backups = readdirSync(folder)
    .filter((name) => /^slate\.before-.+\.sqlite$/.test(name))
    .sort();
  for (const old of backups.slice(0, -keptBackups)) {
    rmSync(join(folder, old));
  }
}

interface SourcesChange {
  addSources: NewSource[];
  removeSources: string[];
  replaceSource: UpdateItemInput["replaceSource"];
}

function withIds(sources: NewSource[]): ItemSource[] {
  return sources.map((source) => ({ ...source, id: randomUUID() }));
}

function changedSources(
  sources: ItemSource[],
  { addSources, removeSources, replaceSource }: SourcesChange,
): ItemSource[] {
  const ids = sources.map((source) => source.id);
  for (const id of [
    ...removeSources,
    ...(replaceSource ? [replaceSource.id] : []),
  ]) {
    if (!ids.includes(id)) {
      throw new SourceChangeError(`The item has no source with the id ${id}`);
    }
  }

  let changed = sources;
  if (replaceSource) {
    const { id, to } = replaceSource;
    const repeated = sources.find(
      (source) => source.id !== id && sourceKey(source) === sourceKey(to),
    );
    if (repeated) {
      throw new SourceChangeError(
        `The item already has another source, ${repeated.id}, with the url or text ${sourceKey(to)}`,
      );
    }
    changed = changed.map((source) =>
      source.id === id ? { ...to, id } : source,
    );
  }
  changed = changed.filter((source) => !removeSources.includes(source.id));
  return withoutRepeats([...changed, ...withIds(addSources)]);
}

type Transaction = Parameters<
  Parameters<NodeSQLiteDatabase["transaction"]>[0]
>[0];

interface RecordedChange
  extends Pick<ItemChange, "kind">, Partial<Pick<ItemChange, "status">> {}

function changedFields(previous: Item, changed: Item): RecordedChange[] {
  const kinds: RecordedChange[] = [];
  if (changed.name !== previous.name) {
    kinds.push({ kind: "name" });
  }
  if (changed.description !== previous.description) {
    kinds.push({ kind: "description" });
  }
  if (JSON.stringify(changed.sources) !== JSON.stringify(previous.sources)) {
    kinds.push({ kind: "sources" });
  }
  if (changed.status !== previous.status) {
    kinds.push({ kind: "status", status: changed.status });
  }
  return kinds;
}

function relationPair(id: string, relatedId: string) {
  return id < relatedId
    ? { itemId: id, relatedId }
    : { itemId: relatedId, relatedId: id };
}

export class ItemStore {
  readonly #database: NodeSQLiteDatabase;
  readonly #now: () => Date;
  readonly #actor: Actor;

  constructor(database: NodeSQLiteDatabase, now: () => Date, actor: Actor) {
    this.#database = database;
    this.#now = now;
    this.#actor = actor;
  }

  by(actor: Actor): ItemStore {
    return new ItemStore(this.#database, this.#now, actor);
  }

  changes(id: string): ItemChange[] | undefined {
    const found = this.#database
      .select({ id: item.id })
      .from(item)
      .where(eq(item.id, id))
      .get();
    if (!found) {
      return undefined;
    }

    return this.#database
      .select({
        at: itemChange.at,
        actor: itemChange.actor,
        kind: itemChange.kind,
        status: itemChange.status,
      })
      .from(itemChange)
      .where(eq(itemChange.itemId, id))
      .orderBy(desc(itemChange.id))
      .all();
  }

  list(input: ListItemsInput = {}): Item[] {
    const { statuses, doneTodayOnly, deleted, source, search, orderBy } =
      listItemsInput.parse(input);
    return this.#database
      .select(itemColumns)
      .from(item)
      .where(
        and(
          eq(item.deleted, deleted),
          inArray(item.status, statuses),
          doneTodayOnly ? notDoneBeforeToday(this.#now()) : undefined,
          source === undefined ? undefined : hasSource(source),
          search === undefined ? undefined : matchesSearch(search),
        ),
      )
      .orderBy(...listOrder[orderBy], item.rank)
      .all();
  }

  add(name: string, description: string, sources: NewSource[]): Item {
    const first = this.#database
      .select({ rank: item.rank })
      .from(item)
      .orderBy(asc(item.rank))
      .get();
    const now = this.#now().toISOString();
    const added: Item = {
      id: randomUUID(),
      name,
      description,
      sources: withIds(withoutRepeats(sources)),
      status: "todo",
      rank: generateKeyBetween(null, first?.rank ?? null),
      createdAt: now,
      updatedAt: now,
      doneAt: null,
    };

    this.#database.transaction((transaction) => {
      transaction.insert(item).values(added).run();
      this.#record(transaction, added, [{ kind: "added" }]);
    });
    return added;
  }

  update({
    id,
    updatedAt,
    addSources = [],
    removeSources = [],
    replaceSource,
    ...changes
  }: UpdateItemInput): Item | undefined {
    const found = this.#find(id);
    if (!found) {
      return undefined;
    }
    if (found.updatedAt !== updatedAt) {
      throw new ItemChangedError(id);
    }

    const sources = changedSources(found.sources, {
      addSources,
      removeSources,
      replaceSource,
    });
    const saved = this.#save(
      found,
      { ...found, ...changes, sources },
      updatedAt,
      changedFields(found, { ...found, ...changes, sources }),
    );
    if (!saved) {
      throw new ItemChangedError(id);
    }
    return saved;
  }

  move({ id, neighbourId, position }: MoveItemInput): Item | undefined {
    const moving = this.#find(id);
    const neighbour = this.#find(neighbourId);
    if (!moving || !neighbour) {
      return undefined;
    }
    if (moving.id === neighbour.id) {
      return moving;
    }

    const next = this.#rankBeside(neighbour.rank, position, id);
    const rank =
      position === "before"
        ? generateKeyBetween(next, neighbour.rank)
        : generateKeyBetween(neighbour.rank, next);
    return this.#save(moving, { ...moving, rank }, moving.updatedAt, [
      { kind: "rank" },
    ]);
  }

  delete(id: string): Item | undefined {
    return this.#database.transaction((transaction) => {
      const deleted = transaction
        .update(item)
        .set({ deleted: true, updatedAt: this.#now().toISOString() })
        .where(and(eq(item.id, id), listed))
        .returning(itemColumns)
        .get();
      this.#record(transaction, deleted, [{ kind: "deleted" }]);
      return deleted;
    });
  }

  restore(id: string): Item | undefined {
    return this.#database.transaction((transaction) => {
      const restored = transaction
        .update(item)
        .set({ deleted: false, updatedAt: this.#now().toISOString() })
        .where(and(eq(item.id, id), eq(item.deleted, true)))
        .returning(itemColumns)
        .get();
      this.#record(transaction, restored, [{ kind: "restored" }]);
      return restored ?? this.#find(id);
    });
  }

  relate({ id, relatedId }: RelateItemsInput): Item | undefined {
    return this.#database.transaction((transaction) => {
      const found = this.#find(id);
      if (!found || !this.#find(relatedId)) {
        return undefined;
      }

      transaction
        .insert(itemRelation)
        .values(relationPair(id, relatedId))
        .onConflictDoNothing()
        .run();
      return found;
    });
  }

  unrelate({ id, relatedId }: UnrelateItemsInput): Item | undefined {
    const found = this.#find(id);
    if (!found) {
      return undefined;
    }

    const pair = relationPair(id, relatedId);
    this.#database
      .delete(itemRelation)
      .where(
        and(
          eq(itemRelation.itemId, pair.itemId),
          eq(itemRelation.relatedId, pair.relatedId),
        ),
      )
      .run();
    return found;
  }

  related(id: string): Item[] | undefined {
    if (!this.#find(id)) {
      return undefined;
    }

    return this.#database
      .select(itemColumns)
      .from(itemRelation)
      .innerJoin(
        item,
        or(
          and(eq(itemRelation.itemId, id), eq(item.id, itemRelation.relatedId)),
          and(eq(itemRelation.relatedId, id), eq(item.id, itemRelation.itemId)),
        ),
      )
      .where(listed)
      .orderBy(item.rank)
      .all();
  }

  #find(id: string): Item | undefined {
    return this.#database
      .select(itemColumns)
      .from(item)
      .where(and(eq(item.id, id), listed))
      .get();
  }

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

  #save(
    previous: Item,
    changed: Item,
    seenUpdatedAt: string,
    kinds: RecordedChange[],
  ): Item | undefined {
    const now = this.#now().toISOString();
    const { id, ...fields } = changed;
    const doneAt =
      changed.status === previous.status
        ? {}
        : { doneAt: changed.status === "done" ? now : null };

    return this.#database.transaction((transaction) => {
      const saved = transaction
        .update(item)
        .set({ ...fields, ...doneAt, updatedAt: now })
        .where(and(eq(item.id, id), listed, eq(item.updatedAt, seenUpdatedAt)))
        .returning(itemColumns)
        .get();
      this.#record(transaction, saved, kinds);
      return saved;
    });
  }

  #record(
    transaction: Transaction,
    changed: Item | undefined,
    kinds: RecordedChange[],
  ) {
    if (!changed || kinds.length === 0) {
      return;
    }
    transaction
      .insert(itemChange)
      .values(
        kinds.map((kind) => ({
          itemId: changed.id,
          at: changed.updatedAt,
          actor: this.#actor,
          ...kind,
        })),
      )
      .run();
  }
}
