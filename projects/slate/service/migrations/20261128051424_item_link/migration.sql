CREATE TABLE `item_link` (
	`item_id` text NOT NULL,
	`linked_id` text NOT NULL,
	CONSTRAINT `item_link_pk` PRIMARY KEY(`item_id`, `linked_id`),
	CONSTRAINT `fk_item_link_item_id_item_id_fk` FOREIGN KEY (`item_id`) REFERENCES `item`(`id`),
	CONSTRAINT `fk_item_link_linked_id_item_id_fk` FOREIGN KEY (`linked_id`) REFERENCES `item`(`id`),
	CONSTRAINT "item_link_order" CHECK("item_id" < "linked_id")
) STRICT;
--> statement-breakpoint
CREATE INDEX `item_link_linked_id` ON `item_link` (`linked_id`);
