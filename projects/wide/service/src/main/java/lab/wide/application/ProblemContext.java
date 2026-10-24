package lab.wide.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.SolutionProblem;

public record ProblemContext(
    Problem problem,
    List<ProblemSignal> signals,
    List<SolutionProblem> solutions,
    List<RefinementQuestion> questions,
    List<RefinementDecision> decisions,
    Map<UUID, String> linkedTitles) {}
