-- Edited by hand after generation: the update copies each item's one source into its list of
-- sources before the old column goes.
ALTER TABLE `item` ADD `sources` text DEFAULT '[]' NOT NULL;--> statement-breakpoint
UPDATE `item` SET `sources` = json_array(json(`source`)) WHERE `source` IS NOT NULL;--> statement-breakpoint
ALTER TABLE `item` DROP COLUMN `source`;
