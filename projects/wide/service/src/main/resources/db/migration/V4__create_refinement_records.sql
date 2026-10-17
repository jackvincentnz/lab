CREATE TABLE problem (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL CHECK (length(btrim(title)) > 0),
    description TEXT NOT NULL CHECK (length(btrim(description)) > 0),
    impact TEXT,
    desired_outcome TEXT,
    open_questions TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX problem_updated_at_idx ON problem (updated_at DESC, id DESC);

CREATE TABLE solution (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL CHECK (length(btrim(title)) > 0),
    approach TEXT NOT NULL CHECK (length(btrim(approach)) > 0),
    scope TEXT,
    tradeoffs TEXT,
    effort TEXT,
    open_questions TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX solution_updated_at_idx ON solution (updated_at DESC, id DESC);

CREATE TABLE problem_signal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    problem_id UUID NOT NULL REFERENCES problem(id),
    signal_id UUID NOT NULL REFERENCES signal(id),
    rationale TEXT NOT NULL CHECK (length(btrim(rationale)) > 0),
    linked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (problem_id, signal_id)
);

CREATE INDEX problem_signal_signal_idx ON problem_signal (signal_id);

CREATE TABLE solution_signal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solution_id UUID NOT NULL REFERENCES solution(id),
    signal_id UUID NOT NULL REFERENCES signal(id),
    rationale TEXT NOT NULL CHECK (length(btrim(rationale)) > 0),
    linked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (solution_id, signal_id)
);

CREATE INDEX solution_signal_signal_idx ON solution_signal (signal_id);

CREATE TABLE solution_problem (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solution_id UUID NOT NULL REFERENCES solution(id),
    problem_id UUID NOT NULL REFERENCES problem(id),
    rationale TEXT NOT NULL CHECK (length(btrim(rationale)) > 0),
    linked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (solution_id, problem_id)
);

CREATE INDEX solution_problem_problem_idx ON solution_problem (problem_id);
