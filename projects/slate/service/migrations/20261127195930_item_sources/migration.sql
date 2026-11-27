-- Edited by hand after generation: the update copies each item's one source into its list of
-- sources, with a random version 4 UUID as its id, before the old column goes.
ALTER TABLE `item` ADD `sources` text DEFAULT '[]' NOT NULL;--> statement-breakpoint
UPDATE `item` SET `sources` = json_array(json_set(json(`source`), '$.id', lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' || substr(lower(hex(randomblob(2))), 2) || '-' || substr('89ab', 1 + abs(random()) % 4, 1) || substr(lower(hex(randomblob(2))), 2) || '-' || lower(hex(randomblob(6))))) WHERE `source` IS NOT NULL;--> statement-breakpoint
ALTER TABLE `item` DROP COLUMN `source`;
