# Database migrations

The service stores its data in SQLite and versions the database's schema with
[Drizzle](https://orm.drizzle.team) migrations:

- [`service/src/schema.ts`](../service/src/schema.ts) declares the tables that the service's
  queries use.
- [`service/migrations`](../service/migrations) holds one folder per schema change. `drizzle-kit`
  generates each folder from the difference between the schema and the previous migration.
- When the service starts, it applies the migrations that the database has not recorded yet, so
  every build runs against the schema it was built with.

## Change the schema

1. Change the tables in `service/src/schema.ts`.
2. Generate the migration from the repository root, naming the change:

   ```zsh
   bazel run //projects/slate/service:generate_migration -- --name=add_due_date
   ```

3. Read the new `migration.sql`, and add `STRICT` to any `CREATE TABLE` in it, because Drizzle cannot
   declare it. `drizzle-kit` recreates a table for changes that SQLite cannot make in place, such as
   a new constraint.
4. In [`service/src/migrations.test.ts`](../service/src/migrations.test.ts), add the new folder's
   name to `migrations`, and add a case to `earlierSchemas` that builds a database in the schema
   before your change, with rows, so that the test proves the migration keeps them.

Never change a migration after a release includes it. A database that already recorded the
migration does not run it again, so the change would never reach that database. Add a new migration
instead.
