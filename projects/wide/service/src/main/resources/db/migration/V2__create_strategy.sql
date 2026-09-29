CREATE TABLE strategy (
    id BIGINT PRIMARY KEY CHECK (id = 1),
    content TEXT CHECK (content IS NULL OR length(btrim(content)) > 0),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Reserve the single current strategy without supplying any initial objectives.
INSERT INTO strategy (id) VALUES (1);
