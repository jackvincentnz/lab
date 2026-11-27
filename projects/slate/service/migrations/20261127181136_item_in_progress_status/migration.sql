-- Edited by hand after generation: STRICT keeps the recreated table rejecting a value of the wrong
-- type, and Drizzle's schema cannot declare it. The copy renames the doing status to in_progress,
-- which the new constraint requires.
PRAGMA foreign_keys=OFF;--> statement-breakpoint
CREATE TABLE `__new_item` (
	`id` text PRIMARY KEY,
	`name` text NOT NULL,
	`description` text DEFAULT '' NOT NULL,
	`source` text,
	`status` text NOT NULL,
	`rank` text NOT NULL UNIQUE,
	`created_at` text NOT NULL,
	`updated_at` text NOT NULL,
	`deleted` integer DEFAULT false NOT NULL,
	`done_at` text,
	CONSTRAINT "item_status" CHECK("status" IN ('todo', 'in_progress', 'done', 'deferred', 'wont_do', 'delegated'))
) STRICT;
--> statement-breakpoint
INSERT INTO `__new_item`(`id`, `name`, `description`, `source`, `status`, `rank`, `created_at`, `updated_at`, `deleted`, `done_at`) SELECT `id`, `name`, `description`, `source`, CASE `status` WHEN 'doing' THEN 'in_progress' ELSE `status` END, `rank`, `created_at`, `updated_at`, `deleted`, `done_at` FROM `item`;--> statement-breakpoint
DROP TABLE `item`;--> statement-breakpoint
ALTER TABLE `__new_item` RENAME TO `item`;--> statement-breakpoint
PRAGMA foreign_keys=ON;
