package lab.wide.api;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import java.util.List;
import java.util.UUID;
import lab.wide.api.gql.types.Problem;
import lab.wide.api.gql.types.ProblemSignal;
import lab.wide.api.gql.types.RefinementDecision;
import lab.wide.api.gql.types.RefinementQuestion;
import lab.wide.api.gql.types.Signal;
import lab.wide.api.gql.types.Solution;
import lab.wide.api.gql.types.SolutionProblem;
import lab.wide.api.gql.types.SolutionSignal;
import lab.wide.api.gql.types.Strategy;
import lab.wide.application.RefinementService;
import lab.wide.application.SignalService;
import lab.wide.application.StrategyService;

/** Read adapter; MCP and the explorer share the same records and services. */
@DgsComponent
public class ExplorerDataFetcher {
  private final SignalService signals;
  private final RefinementService refinement;
  private final StrategyService strategyService;
  private final ExplorerMapper mapper;

  public ExplorerDataFetcher(
      SignalService signals,
      RefinementService refinement,
      StrategyService strategyService,
      ExplorerMapper mapper) {
    this.signals = signals;
    this.refinement = refinement;
    this.strategyService = strategyService;
    this.mapper = mapper;
  }

  @DgsQuery
  public List<Signal> signals(
      @InputArgument String query, @InputArgument Integer limit, @InputArgument Integer page) {
    return signals.list(query, limit, page).stream().map(mapper::map).toList();
  }

  @DgsQuery
  public Signal signal(@InputArgument String id) {
    return mapper.map(signals.get(UUID.fromString(id)));
  }

  @DgsQuery
  public List<Problem> problems(@InputArgument Integer limit, @InputArgument Integer page) {
    return refinement.listProblems(limit, page).stream().map(mapper::map).toList();
  }

  @DgsQuery
  public Problem problem(@InputArgument String id) {
    return mapper.map(refinement.getProblem(UUID.fromString(id)).problem());
  }

  @DgsQuery
  public List<Solution> solutions(@InputArgument Integer limit, @InputArgument Integer page) {
    return refinement.listSolutions(limit, page).stream().map(mapper::map).toList();
  }

  @DgsQuery
  public Solution solution(@InputArgument String id) {
    return mapper.map(refinement.getSolution(UUID.fromString(id)).solution());
  }

  @DgsQuery
  public Strategy strategy() {
    return mapper.map(strategyService.read());
  }

  @DgsData(parentType = "Problem", field = "questions")
  public List<RefinementQuestion> problemQuestions(DgsDataFetchingEnvironment env) {
    return refinement.getProblem(problemId(env)).questions().stream().map(mapper::map).toList();
  }

  @DgsData(parentType = "Problem", field = "decisions")
  public List<RefinementDecision> problemDecisions(DgsDataFetchingEnvironment env) {
    return refinement.getProblem(problemId(env)).decisions().stream().map(mapper::map).toList();
  }

  @DgsData(parentType = "Solution", field = "questions")
  public List<RefinementQuestion> solutionQuestions(DgsDataFetchingEnvironment env) {
    return refinement.getSolution(solutionId(env)).questions().stream().map(mapper::map).toList();
  }

  @DgsData(parentType = "Solution", field = "decisions")
  public List<RefinementDecision> solutionDecisions(DgsDataFetchingEnvironment env) {
    return refinement.getSolution(solutionId(env)).decisions().stream().map(mapper::map).toList();
  }

  @DgsData(parentType = "Problem", field = "signals")
  public List<ProblemSignal> problemSignals(DgsDataFetchingEnvironment env) {
    return refinement.getProblem(problemId(env)).signals().stream().map(mapper::map).toList();
  }

  @DgsData(parentType = "Problem", field = "solutions")
  public List<SolutionProblem> problemSolutions(DgsDataFetchingEnvironment env) {
    return refinement.getProblem(problemId(env)).solutions().stream().map(mapper::map).toList();
  }

  @DgsData(parentType = "Solution", field = "signals")
  public List<SolutionSignal> solutionSignals(DgsDataFetchingEnvironment env) {
    return refinement.getSolution(solutionId(env)).signals().stream().map(mapper::map).toList();
  }

  @DgsData(parentType = "Solution", field = "problems")
  public List<SolutionProblem> solutionProblems(DgsDataFetchingEnvironment env) {
    return refinement.getSolution(solutionId(env)).problems().stream().map(mapper::map).toList();
  }

  @DgsData(parentType = "Signal", field = "problems")
  public List<ProblemSignal> signalProblems(DgsDataFetchingEnvironment env) {
    Signal signal = env.getSource();
    return refinement.problemsForSignal(UUID.fromString(signal.getId())).stream()
        .map(mapper::map)
        .toList();
  }

  @DgsData(parentType = "Signal", field = "solutions")
  public List<SolutionSignal> signalSolutions(DgsDataFetchingEnvironment env) {
    Signal signal = env.getSource();
    return refinement.solutionsForSignal(UUID.fromString(signal.getId())).stream()
        .map(mapper::map)
        .toList();
  }

  @DgsData(parentType = "ProblemSignal", field = "problem")
  public Problem sourceProblem(DgsDataFetchingEnvironment env) {
    var link = refinement.problemSignal(problemSignalId(env));
    return mapper.map(refinement.getProblem(link.problemId()).problem());
  }

  @DgsData(parentType = "ProblemSignal", field = "signal")
  public Signal problemSource(DgsDataFetchingEnvironment env) {
    var link = refinement.problemSignal(problemSignalId(env));
    return mapper.map(signals.get(link.signalId()));
  }

  @DgsData(parentType = "SolutionSignal", field = "solution")
  public Solution sourceSolution(DgsDataFetchingEnvironment env) {
    var link = refinement.solutionSignal(solutionSignalId(env));
    return mapper.map(refinement.getSolution(link.solutionId()).solution());
  }

  @DgsData(parentType = "SolutionSignal", field = "signal")
  public Signal solutionSource(DgsDataFetchingEnvironment env) {
    var link = refinement.solutionSignal(solutionSignalId(env));
    return mapper.map(signals.get(link.signalId()));
  }

  @DgsData(parentType = "SolutionProblem", field = "problem")
  public Problem addressedProblem(DgsDataFetchingEnvironment env) {
    var link = refinement.solutionProblem(solutionProblemId(env));
    return mapper.map(refinement.getProblem(link.problemId()).problem());
  }

  @DgsData(parentType = "SolutionProblem", field = "solution")
  public Solution candidate(DgsDataFetchingEnvironment env) {
    var link = refinement.solutionProblem(solutionProblemId(env));
    return mapper.map(refinement.getSolution(link.solutionId()).solution());
  }

  private static UUID problemId(DgsDataFetchingEnvironment env) {
    Problem problem = env.getSource();
    return UUID.fromString(problem.getId());
  }

  private static UUID solutionId(DgsDataFetchingEnvironment env) {
    Solution solution = env.getSource();
    return UUID.fromString(solution.getId());
  }

  // Link types expose only the relationship id; endpoints come from the stored link.
  private static UUID problemSignalId(DgsDataFetchingEnvironment env) {
    ProblemSignal link = env.getSource();
    return UUID.fromString(link.getId());
  }

  private static UUID solutionSignalId(DgsDataFetchingEnvironment env) {
    SolutionSignal link = env.getSource();
    return UUID.fromString(link.getId());
  }

  private static UUID solutionProblemId(DgsDataFetchingEnvironment env) {
    SolutionProblem link = env.getSource();
    return UUID.fromString(link.getId());
  }
}
