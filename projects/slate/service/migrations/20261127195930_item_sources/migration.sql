ALTER TABLE `item` ADD `sources` text DEFAULT '[]' NOT NULL;--> statement-breakpoint
ALTER TABLE `item` DROP COLUMN `source`;
