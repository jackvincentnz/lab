-- Existing text may include summaries; do not infer provenance retrospectively.
ALTER TABLE signal ADD COLUMN kind TEXT NOT NULL DEFAULT 'UNSPECIFIED';
ALTER TABLE signal ADD CONSTRAINT signal_kind_check
    CHECK (kind IN ('ORIGINAL', 'EXCERPT', 'ANALYSIS', 'UNSPECIFIED'));
