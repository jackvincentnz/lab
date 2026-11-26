ALTER TABLE `item` ADD `done_at` text;--> statement-breakpoint
UPDATE `item` SET `done_at` = `updated_at` WHERE `status` = 'done';
