CREATE TABLE signal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL CHECK (length(btrim(title)) > 0),
    content TEXT NOT NULL CHECK (length(btrim(content)) > 0),
    source TEXT,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX signal_captured_at_idx ON signal (captured_at DESC, id DESC);

CREATE TABLE strategy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT CHECK (content IS NULL OR length(btrim(content)) > 0),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Keep one current document independently of its generated identifier.
CREATE UNIQUE INDEX strategy_singleton_idx ON strategy ((true));

-- Reserve the single current strategy without supplying any initial objectives.
INSERT INTO strategy DEFAULT VALUES;

CREATE TABLE problem (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL CHECK (length(btrim(title)) > 0),
    description TEXT NOT NULL CHECK (length(btrim(description)) > 0),
    impact TEXT,
    desired_outcome TEXT,
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

CREATE TABLE refinement_question (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    problem_id UUID REFERENCES problem(id),
    solution_id UUID REFERENCES solution(id),
    question TEXT NOT NULL CHECK (length(btrim(question)) > 0),
    status TEXT NOT NULL CHECK (status IN ('OPEN', 'ANSWERED')),
    answer TEXT,
    context TEXT,
    source TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK (num_nonnulls(problem_id, solution_id) = 1),
    CHECK (status <> 'ANSWERED' OR (answer IS NOT NULL AND length(btrim(answer)) > 0))
);

CREATE INDEX refinement_question_problem_idx ON refinement_question (problem_id);
CREATE INDEX refinement_question_solution_idx ON refinement_question (solution_id);

CREATE TABLE refinement_decision (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    problem_id UUID REFERENCES problem(id),
    solution_id UUID REFERENCES solution(id),
    decision TEXT NOT NULL CHECK (length(btrim(decision)) > 0),
    rationale TEXT,
    source TEXT,
    revisit_when TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CHECK (num_nonnulls(problem_id, solution_id) = 1)
);

CREATE INDEX refinement_decision_problem_idx ON refinement_decision (problem_id);
CREATE INDEX refinement_decision_solution_idx ON refinement_decision (solution_id);
