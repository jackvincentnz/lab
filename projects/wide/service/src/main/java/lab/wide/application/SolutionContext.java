package lab.wide.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Solution;
import lab.wide.domain.SolutionProblem;
import lab.wide.domain.SolutionSignal;

public record SolutionContext(
    Solution solution,
    List<SolutionSignal> signals,
    List<SolutionProblem> problems,
    List<RefinementQuestion> questions,
    List<RefinementDecision> decisions,
    Map<UUID, String> linkedTitles) {}
