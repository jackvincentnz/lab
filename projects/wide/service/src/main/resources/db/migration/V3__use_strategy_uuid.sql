ALTER TABLE strategy DROP CONSTRAINT strategy_id_check;
ALTER TABLE strategy ALTER COLUMN id TYPE UUID USING gen_random_uuid();
ALTER TABLE strategy ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- Keep one current document independently of its generated identifier.
CREATE UNIQUE INDEX strategy_singleton_idx ON strategy ((true));
