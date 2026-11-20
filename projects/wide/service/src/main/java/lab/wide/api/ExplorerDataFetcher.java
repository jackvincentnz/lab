package lab.wide.api;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.exceptions.DgsBadRequestException;
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
  private final SignalService signalService;
  private final RefinementService refinementService;
  private final StrategyService strategyService;
  private final ExplorerMapper explorerMapper;

  public ExplorerDataFetcher(
      SignalService signalService,
      RefinementService refinementService,
      StrategyService strategyService,
      ExplorerMapper explorerMapper) {
    this.signalService = signalService;
    this.refinementService = refinementService;
    this.strategyService = strategyService;
    this.explorerMapper = explorerMapper;
  }

  @DgsQuery
  public List<Signal> signals(
      @InputArgument String query, @InputArgument Integer limit, @InputArgument Integer page) {
    return signalService.list(query, limit, page).stream().map(explorerMapper::map).toList();
  }

  @DgsQuery
  public Signal signal(@InputArgument String id) {
    return signalService.find(id(id)).map(explorerMapper::map).orElse(null);
  }

  // Unlike an unknown id, which the nullable field answers, a malformed id is the caller's error.
  private static UUID id(String id) {
    try {
      return UUID.fromString(id);
    } catch (IllegalArgumentException illegalArgumentException) {
      throw new DgsBadRequestException("Malformed id: " + id);
    }
  }

  @DgsQuery
  public List<Problem> problems(@InputArgument Integer limit, @InputArgument Integer page) {
    return refinementService.listProblems(limit, page).stream().map(explorerMapper::map).toList();
  }

  @DgsQuery
  public Problem problem(@InputArgument String id) {
    return refinementService.findProblem(id(id)).map(explorerMapper::map).orElse(null);
  }

  @DgsQuery
  public List<Solution> solutions(@InputArgument Integer limit, @InputArgument Integer page) {
    return refinementService.listSolutions(limit, page).stream().map(explorerMapper::map).toList();
  }

  @DgsQuery
  public Solution solution(@InputArgument String id) {
    return refinementService.findSolution(id(id)).map(explorerMapper::map).orElse(null);
  }

  @DgsQuery
  public Strategy strategy() {
    return explorerMapper.map(strategyService.read());
  }

  @DgsData(parentType = "Problem", field = "questions")
  public List<RefinementQuestion> problemQuestions(
      DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    return refinementService.getProblem(problemId(dgsDataFetchingEnvironment)).questions().stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Problem", field = "decisions")
  public List<RefinementDecision> problemDecisions(
      DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    return refinementService.getProblem(problemId(dgsDataFetchingEnvironment)).decisions().stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Solution", field = "questions")
  public List<RefinementQuestion> solutionQuestions(
      DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    return refinementService
        .getSolution(solutionId(dgsDataFetchingEnvironment))
        .questions()
        .stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Solution", field = "decisions")
  public List<RefinementDecision> solutionDecisions(
      DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    return refinementService
        .getSolution(solutionId(dgsDataFetchingEnvironment))
        .decisions()
        .stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Problem", field = "signals")
  public List<ProblemSignal> problemSignals(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    return refinementService.getProblem(problemId(dgsDataFetchingEnvironment)).signals().stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Problem", field = "solutions")
  public List<SolutionProblem> problemSolutions(
      DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    return refinementService.getProblem(problemId(dgsDataFetchingEnvironment)).solutions().stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Solution", field = "signals")
  public List<SolutionSignal> solutionSignals(
      DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    return refinementService.getSolution(solutionId(dgsDataFetchingEnvironment)).signals().stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Solution", field = "problems")
  public List<SolutionProblem> solutionProblems(
      DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    return refinementService.getSolution(solutionId(dgsDataFetchingEnvironment)).problems().stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Signal", field = "problems")
  public List<ProblemSignal> signalProblems(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    Signal signal = dgsDataFetchingEnvironment.getSource();
    return refinementService.problemsForSignal(UUID.fromString(signal.getId())).stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "Signal", field = "solutions")
  public List<SolutionSignal> signalSolutions(
      DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    Signal signal = dgsDataFetchingEnvironment.getSource();
    return refinementService.solutionsForSignal(UUID.fromString(signal.getId())).stream()
        .map(explorerMapper::map)
        .toList();
  }

  @DgsData(parentType = "ProblemSignal", field = "problem")
  public Problem sourceProblem(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    var problemSignal =
        refinementService.problemSignal(problemSignalId(dgsDataFetchingEnvironment));
    return explorerMapper.map(refinementService.getProblem(problemSignal.problemId()).problem());
  }

  @DgsData(parentType = "ProblemSignal", field = "signal")
  public Signal problemSource(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    var problemSignal =
        refinementService.problemSignal(problemSignalId(dgsDataFetchingEnvironment));
    return explorerMapper.map(signalService.get(problemSignal.signalId()));
  }

  @DgsData(parentType = "SolutionSignal", field = "solution")
  public Solution sourceSolution(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    var solutionSignal =
        refinementService.solutionSignal(solutionSignalId(dgsDataFetchingEnvironment));
    return explorerMapper.map(
        refinementService.getSolution(solutionSignal.solutionId()).solution());
  }

  @DgsData(parentType = "SolutionSignal", field = "signal")
  public Signal solutionSource(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    var solutionSignal =
        refinementService.solutionSignal(solutionSignalId(dgsDataFetchingEnvironment));
    return explorerMapper.map(signalService.get(solutionSignal.signalId()));
  }

  @DgsData(parentType = "SolutionProblem", field = "problem")
  public Problem addressedProblem(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    var solutionProblem =
        refinementService.solutionProblem(solutionProblemId(dgsDataFetchingEnvironment));
    return explorerMapper.map(refinementService.getProblem(solutionProblem.problemId()).problem());
  }

  @DgsData(parentType = "SolutionProblem", field = "solution")
  public Solution candidate(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    var solutionProblem =
        refinementService.solutionProblem(solutionProblemId(dgsDataFetchingEnvironment));
    return explorerMapper.map(
        refinementService.getSolution(solutionProblem.solutionId()).solution());
  }

  private static UUID problemId(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    Problem problem = dgsDataFetchingEnvironment.getSource();
    return UUID.fromString(problem.getId());
  }

  private static UUID solutionId(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    Solution solution = dgsDataFetchingEnvironment.getSource();
    return UUID.fromString(solution.getId());
  }

  // Link types expose only the relationship id; endpoints come from the stored link.
  private static UUID problemSignalId(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    ProblemSignal problemSignal = dgsDataFetchingEnvironment.getSource();
    return UUID.fromString(problemSignal.getId());
  }

  private static UUID solutionSignalId(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    SolutionSignal solutionSignal = dgsDataFetchingEnvironment.getSource();
    return UUID.fromString(solutionSignal.getId());
  }

  private static UUID solutionProblemId(DgsDataFetchingEnvironment dgsDataFetchingEnvironment) {
    SolutionProblem solutionProblem = dgsDataFetchingEnvironment.getSource();
    return UUID.fromString(solutionProblem.getId());
  }
}
