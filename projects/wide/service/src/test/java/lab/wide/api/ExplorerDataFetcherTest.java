package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.exceptions.DgsBadRequestException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.api.gql.types.Problem;
import lab.wide.api.gql.types.RefinementDecision;
import lab.wide.api.gql.types.RefinementQuestion;
import lab.wide.api.gql.types.SolutionSignal;
import lab.wide.application.ProblemContext;
import lab.wide.application.RefinementService;
import lab.wide.application.SignalService;
import lab.wide.application.SolutionContext;
import lab.wide.application.StrategyService;
import lab.wide.domain.QuestionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExplorerDataFetcherTest extends TestBase {

  @Mock SignalService signalService;

  @Mock RefinementService refinementService;

  @Mock StrategyService strategyService;

  @Spy ExplorerMapper explorerMapper;

  @Mock DgsDataFetchingEnvironment dgsDataFetchingEnvironment;

  @InjectMocks ExplorerDataFetcher explorerDataFetcher;

  @Test
  void problems_paging_mapsTheListedProblems() {
    var problem = problem();
    when(refinementService.listProblems(5, 2)).thenReturn(List.of(problem));

    var problems = explorerDataFetcher.problems(5, 2);

    assertThat(problems).extracting(Problem::getId).containsExactly(problem.id().toString());
  }

  @Test
  void problem_unknownId_returnsNull() {
    var id = UUID.randomUUID();
    when(refinementService.findProblem(id)).thenReturn(Optional.empty());

    var problem = explorerDataFetcher.problem(id.toString());

    assertThat(problem).isNull();
  }

  @Test
  void signal_malformedId_throwsBadRequest() {
    var id = randomString();

    assertThatThrownBy(() -> explorerDataFetcher.signal(id))
        .isInstanceOf(DgsBadRequestException.class)
        .hasMessage("Malformed id: " + id);
  }

  @Test
  void problemDecisions_parentProblem_mapsItsDecisions() {
    var problem = sourceProblem();
    var refinementDecision = decision(problem.id(), null);
    when(refinementService.getProblem(problem.id()))
        .thenReturn(
            new ProblemContext(
                problem, List.of(), List.of(), List.of(), List.of(refinementDecision), Map.of()));

    var decisions = explorerDataFetcher.problemDecisions(dgsDataFetchingEnvironment);

    assertThat(decisions)
        .extracting(RefinementDecision::getId)
        .containsExactly(refinementDecision.id().toString());
  }

  @Test
  void solutionQuestions_parentSolution_mapsItsQuestions() {
    var solution = sourceSolution();
    var refinementQuestion = question(null, solution.id());
    when(refinementService.getSolution(solution.id()))
        .thenReturn(
            new SolutionContext(
                solution, List.of(), List.of(), List.of(refinementQuestion), List.of(), Map.of()));

    var questions = explorerDataFetcher.solutionQuestions(dgsDataFetchingEnvironment);

    assertThat(questions)
        .extracting(RefinementQuestion::getId)
        .containsExactly(refinementQuestion.id().toString());
  }

  @Test
  void solutionSignals_parentSolution_mapsItsSignalLinks() {
    var solution = sourceSolution();
    var solutionSignal = solutionSignal(solution.id(), UUID.randomUUID());
    when(refinementService.getSolution(solution.id()))
        .thenReturn(
            new SolutionContext(
                solution, List.of(solutionSignal), List.of(), List.of(), List.of(), Map.of()));

    var solutionSignals = explorerDataFetcher.solutionSignals(dgsDataFetchingEnvironment);

    assertThat(solutionSignals)
        .extracting(SolutionSignal::getId)
        .containsExactly(solutionSignal.id().toString());
  }

  @Test
  void solutionSource_solutionSignalLink_readsTheSignalThroughTheStoredLink() {
    var signal = signal();
    var solutionSignal = sourceSolutionSignal(UUID.randomUUID(), signal.id());
    when(signalService.get(solutionSignal.signalId())).thenReturn(signal);

    var fetched = explorerDataFetcher.solutionSource(dgsDataFetchingEnvironment);

    assertThat(fetched.getId()).isEqualTo(signal.id().toString());
  }

  private lab.wide.domain.Problem sourceProblem() {
    var problem = problem();
    var source = explorerMapper.map(problem);
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(source);
    return problem;
  }

  private lab.wide.domain.Solution sourceSolution() {
    var solution = solution();
    var source = explorerMapper.map(solution);
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(source);
    return solution;
  }

  private lab.wide.domain.SolutionSignal sourceSolutionSignal(UUID solutionId, UUID signalId) {
    var solutionSignal = solutionSignal(solutionId, signalId);
    var source = explorerMapper.map(solutionSignal);
    when(dgsDataFetchingEnvironment.getSource()).thenReturn(source);
    when(refinementService.solutionSignal(solutionSignal.id())).thenReturn(solutionSignal);
    return solutionSignal;
  }

  private lab.wide.domain.Signal signal() {
    return new lab.wide.domain.Signal(
        UUID.randomUUID(), randomString(), randomString(), null, Instant.now());
  }

  private lab.wide.domain.Problem problem() {
    return new lab.wide.domain.Problem(
        UUID.randomUUID(),
        randomString(),
        randomString(),
        null,
        null,
        Instant.now(),
        Instant.now());
  }

  private lab.wide.domain.Solution solution() {
    return new lab.wide.domain.Solution(
        UUID.randomUUID(),
        randomString(),
        randomString(),
        null,
        null,
        null,
        Instant.now(),
        Instant.now());
  }

  private lab.wide.domain.SolutionSignal solutionSignal(UUID solutionId, UUID signalId) {
    return new lab.wide.domain.SolutionSignal(
        UUID.randomUUID(), solutionId, signalId, randomString(), Instant.now());
  }

  private lab.wide.domain.RefinementQuestion question(UUID problemId, UUID solutionId) {
    return new lab.wide.domain.RefinementQuestion(
        UUID.randomUUID(),
        problemId,
        solutionId,
        randomString(),
        QuestionStatus.OPEN,
        null,
        null,
        null,
        Instant.now(),
        Instant.now());
  }

  private lab.wide.domain.RefinementDecision decision(UUID problemId, UUID solutionId) {
    return new lab.wide.domain.RefinementDecision(
        UUID.randomUUID(),
        problemId,
        solutionId,
        randomString(),
        null,
        null,
        null,
        Instant.now(),
        Instant.now());
  }
}
