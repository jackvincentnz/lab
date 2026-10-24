-- Preserve the old free-text material verbatim, without inferring questions or decisions.
UPDATE problem
SET description = description || E'\n\n### Earlier refinement context\n\n' || open_questions
WHERE open_questions IS NOT NULL AND open_questions <> '';

UPDATE solution
SET approach = approach || E'\n\n### Earlier refinement context\n\n' || open_questions
WHERE open_questions IS NOT NULL AND open_questions <> '';

ALTER TABLE problem DROP COLUMN open_questions;
ALTER TABLE solution DROP COLUMN open_questions;
