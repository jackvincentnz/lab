-- Edited by hand after generation:
-- - IF NOT EXISTS lets a database created by a build from before Slate had migrations, which already
--   has this table, record this migration and keep its rows.
-- - STRICT makes SQLite reject a value of the wrong type, and Drizzle's schema cannot declare it.
CREATE TABLE IF NOT EXISTS `item` (
	`id` text PRIMARY KEY,
	`name` text NOT NULL,
	`status` text NOT NULL,
	`rank` text NOT NULL UNIQUE,
	`created_at` text NOT NULL,
	`updated_at` text NOT NULL,
	CONSTRAINT "item_status" CHECK("status" IN ('todo', 'doing', 'done'))
) STRICT;
