-- Keep saved signal content, sources, IDs, timestamps, and relationships intact.
ALTER TABLE signal DROP COLUMN kind;

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
CREATE INDEX refinement_question_problem_idx ON refinement_question(problem_id);
CREATE INDEX refinement_question_solution_idx ON refinement_question(solution_id);

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
CREATE INDEX refinement_decision_problem_idx ON refinement_decision(problem_id);
CREATE INDEX refinement_decision_solution_idx ON refinement_decision(solution_id);

-- Earlier open_questions text can contain questions, answers, decisions, and hypotheses.
-- Preserve it verbatim; separate items deliberately during later refinement, not by guessing in SQL.
