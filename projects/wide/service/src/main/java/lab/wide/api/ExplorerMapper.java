package lab.wide.api;

import java.time.Instant;
import java.util.UUID;
import lab.wide.api.gql.types.Problem;
import lab.wide.api.gql.types.ProblemSignal;
import lab.wide.api.gql.types.QuestionStatus;
import lab.wide.api.gql.types.RefinementDecision;
import lab.wide.api.gql.types.RefinementQuestion;
import lab.wide.api.gql.types.Signal;
import lab.wide.api.gql.types.Solution;
import lab.wide.api.gql.types.SolutionProblem;
import lab.wide.api.gql.types.SolutionSignal;
import lab.wide.api.gql.types.Strategy;
import org.springframework.stereotype.Component;

/** Maps stored records to the generated GraphQL types; nested lists are resolved per field. */
@Component
public class ExplorerMapper {

  public Signal map(lab.wide.domain.Signal signal) {
    return Signal.newBuilder()
        .id(id(signal.id()))
        .title(signal.title())
        .content(signal.content())
        .source(signal.source())
        .capturedAt(time(signal.capturedAt()))
        .build();
  }

  public Problem map(lab.wide.domain.Problem problem) {
    return Problem.newBuilder()
        .id(id(problem.id()))
        .title(problem.title())
        .description(problem.description())
        .impact(problem.impact())
        .desiredOutcome(problem.desiredOutcome())
        .createdAt(time(problem.createdAt()))
        .updatedAt(time(problem.updatedAt()))
        .build();
  }

  public Solution map(lab.wide.domain.Solution solution) {
    return Solution.newBuilder()
        .id(id(solution.id()))
        .title(solution.title())
        .approach(solution.approach())
        .scope(solution.scope())
        .tradeoffs(solution.tradeoffs())
        .effort(solution.effort())
        .createdAt(time(solution.createdAt()))
        .updatedAt(time(solution.updatedAt()))
        .build();
  }

  public Strategy map(lab.wide.domain.Strategy strategy) {
    return Strategy.newBuilder()
        .id(id(strategy.id()))
        .content(strategy.content())
        .updatedAt(time(strategy.updatedAt()))
        .build();
  }

  public ProblemSignal map(lab.wide.domain.ProblemSignal problemSignal) {
    return ProblemSignal.newBuilder()
        .id(id(problemSignal.id()))
        .rationale(problemSignal.rationale())
        .linkedAt(time(problemSignal.linkedAt()))
        .build();
  }

  public SolutionSignal map(lab.wide.domain.SolutionSignal solutionSignal) {
    return SolutionSignal.newBuilder()
        .id(id(solutionSignal.id()))
        .rationale(solutionSignal.rationale())
        .linkedAt(time(solutionSignal.linkedAt()))
        .build();
  }

  public SolutionProblem map(lab.wide.domain.SolutionProblem solutionProblem) {
    return SolutionProblem.newBuilder()
        .id(id(solutionProblem.id()))
        .rationale(solutionProblem.rationale())
        .linkedAt(time(solutionProblem.linkedAt()))
        .build();
  }

  public RefinementQuestion map(lab.wide.domain.RefinementQuestion refinementQuestion) {
    return RefinementQuestion.newBuilder()
        .id(id(refinementQuestion.id()))
        .problemId(id(refinementQuestion.problemId()))
        .solutionId(id(refinementQuestion.solutionId()))
        .question(refinementQuestion.question())
        .status(QuestionStatus.valueOf(refinementQuestion.status().name()))
        .answer(refinementQuestion.answer())
        .context(refinementQuestion.context())
        .source(refinementQuestion.source())
        .createdAt(time(refinementQuestion.createdAt()))
        .updatedAt(time(refinementQuestion.updatedAt()))
        .build();
  }

  public RefinementDecision map(lab.wide.domain.RefinementDecision refinementDecision) {
    return RefinementDecision.newBuilder()
        .id(id(refinementDecision.id()))
        .problemId(id(refinementDecision.problemId()))
        .solutionId(id(refinementDecision.solutionId()))
        .decision(refinementDecision.decision())
        .rationale(refinementDecision.rationale())
        .source(refinementDecision.source())
        .revisitWhen(refinementDecision.revisitWhen())
        .createdAt(time(refinementDecision.createdAt()))
        .updatedAt(time(refinementDecision.updatedAt()))
        .build();
  }

  private static String id(UUID id) {
    return id == null ? null : id.toString();
  }

  private static String time(Instant instant) {
    return instant == null ? null : instant.toString();
  }
}
