CREATE TABLE `item_relation` (
	`item_id` text NOT NULL,
	`related_id` text NOT NULL,
	CONSTRAINT `item_relation_pk` PRIMARY KEY(`item_id`, `related_id`),
	CONSTRAINT `fk_item_relation_item_id_item_id_fk` FOREIGN KEY (`item_id`) REFERENCES `item`(`id`),
	CONSTRAINT `fk_item_relation_related_id_item_id_fk` FOREIGN KEY (`related_id`) REFERENCES `item`(`id`),
	CONSTRAINT "item_relation_order" CHECK("item_id" < "related_id")
) STRICT;
--> statement-breakpoint
CREATE INDEX `item_relation_related_id` ON `item_relation` (`related_id`);
