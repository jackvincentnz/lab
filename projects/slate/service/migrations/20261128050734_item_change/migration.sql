CREATE TABLE `item_change` (
	`id` integer PRIMARY KEY AUTOINCREMENT,
	`item_id` text NOT NULL,
	`at` text NOT NULL,
	`actor` text NOT NULL,
	`kind` text NOT NULL,
	`status` text,
	CONSTRAINT `fk_item_change_item_id_item_id_fk` FOREIGN KEY (`item_id`) REFERENCES `item`(`id`),
	CONSTRAINT "item_change_actor" CHECK("actor" IN ('user', 'agent')),
	CONSTRAINT "item_change_kind" CHECK("kind" IN ('added', 'name', 'description', 'sources', 'status', 'rank', 'deleted', 'restored')),
	CONSTRAINT "item_change_status_value" CHECK("status" IN ('todo', 'in_progress', 'done', 'deferred', 'wont_do', 'delegated')),
	CONSTRAINT "item_change_status" CHECK(("kind" = 'status') = ("status" IS NOT NULL))
) STRICT;
--> statement-breakpoint
CREATE INDEX `item_change_item_id` ON `item_change` (`item_id`);
