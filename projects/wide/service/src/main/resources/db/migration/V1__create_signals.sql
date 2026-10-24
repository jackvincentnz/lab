CREATE TABLE signal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL CHECK (length(btrim(title)) > 0),
    content TEXT NOT NULL CHECK (length(btrim(content)) > 0),
    source TEXT,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX signal_captured_at_idx ON signal (captured_at DESC, id DESC);
