package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.api.gql.types.Problem;
import lab.wide.api.gql.types.ProblemSignal;
import lab.wide.api.gql.types.Signal;
import lab.wide.api.gql.types.SolutionProblem;
import lab.wide.application.ProblemContext;
import lab.wide.application.RefinementService;
import lab.wide.application.SignalService;
import lab.wide.application.SolutionContext;
import lab.wide.application.StrategyService;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Solution;
import lab.wide.domain.Strategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExplorerDataFetcherTest extends TestBase {

  @Mock SignalService signals;

  @Mock RefinementService refinement;

  @Mock StrategyService strategy;

  @Mock DgsDataFetchingEnvironment env;

  @Test
  void signal_parsesTheIdAndMapsTheRecord() {
    var signal =
        new lab.wide.domain.Signal(
            UUID.randomUUID(), randomString(), randomString(), null, Instant.now());
    when(signals.get(signal.id())).thenReturn(signal);

    var fetched = fetcher().signal(signal.id().toString());

    assertThat(fetched.getId()).isEqualTo(signal.id().toString());
    assertThat(fetched.getContent()).isEqualTo(signal.content());
  }

  @Test
  void signals_passesQueryAndPagingThrough() {
    var query = randomString();
    when(signals.list(query, 5, 2)).thenReturn(List.of());

    assertThat(fetcher().signals(query, 5, 2)).isEmpty();
  }

  @Test
  void strategy_mapsTheCurrentDocument() {
    var current = new Strategy(UUID.randomUUID(), randomString(), Instant.now());
    when(strategy.read()).thenReturn(current);

    var fetched = fetcher().strategy();

    assertThat(fetched.getContent()).isEqualTo(current.content());
    assertThat(fetched.getUpdatedAt()).isEqualTo(current.updatedAt().toString());
  }

  @Test
  void problemQuestions_resolvesFromTheParentProblemId() {
    var problemId = UUID.randomUUID();
    var question =
        new RefinementQuestion(
            UUID.randomUUID(),
            problemId,
            null,
            randomString(),
            QuestionStatus.OPEN,
            null,
            null,
            null,
            Instant.now(),
            Instant.now());
    when(env.getSource()).thenReturn(Problem.newBuilder().id(problemId.toString()).build());
    when(refinement.getProblem(problemId))
        .thenReturn(
            new ProblemContext(null, List.of(), List.of(), List.of(question), List.of(), Map.of()));

    var resolved = fetcher().problemQuestions(env);

    assertThat(resolved)
        .extracting(lab.wide.api.gql.types.RefinementQuestion::getId)
        .containsExactly(question.id().toString());
  }

  @Test
  void signalProblems_resolvesLinksForTheSourceSignal() {
    var signalId = UUID.randomUUID();
    var link =
        new lab.wide.domain.ProblemSignal(
            UUID.randomUUID(), UUID.randomUUID(), signalId, randomString(), Instant.now());
    when(env.getSource()).thenReturn(Signal.newBuilder().id(signalId.toString()).build());
    when(refinement.problemsForSignal(signalId)).thenReturn(List.of(link));

    var resolved = fetcher().signalProblems(env);

    assertThat(resolved).extracting(ProblemSignal::getId).containsExactly(link.id().toString());
  }

  @Test
  void problemSource_readsTheSignalThroughTheStoredLink() {
    var link =
        new lab.wide.domain.ProblemSignal(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), randomString(), Instant.now());
    var signal =
        new lab.wide.domain.Signal(
            link.signalId(), randomString(), randomString(), null, Instant.now());
    when(env.getSource()).thenReturn(ProblemSignal.newBuilder().id(link.id().toString()).build());
    when(refinement.problemSignal(link.id())).thenReturn(link);
    when(signals.get(link.signalId())).thenReturn(signal);

    var resolved = fetcher().problemSource(env);

    assertThat(resolved.getId()).isEqualTo(signal.id().toString());
  }

  @Test
  void candidate_readsTheSolutionThroughTheStoredLink() {
    var link =
        new lab.wide.domain.SolutionProblem(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), randomString(), Instant.now());
    var solution =
        new Solution(
            link.solutionId(),
            randomString(),
            randomString(),
            null,
            null,
            null,
            Instant.now(),
            Instant.now());
    when(env.getSource()).thenReturn(SolutionProblem.newBuilder().id(link.id().toString()).build());
    when(refinement.solutionProblem(link.id())).thenReturn(link);
    when(refinement.getSolution(link.solutionId()))
        .thenReturn(
            new SolutionContext(solution, List.of(), List.of(), List.of(), List.of(), Map.of()));

    var resolved = fetcher().candidate(env);

    assertThat(resolved.getId()).isEqualTo(solution.id().toString());
    assertThat(resolved.getApproach()).isEqualTo(solution.approach());
  }

  private ExplorerDataFetcher fetcher() {
    return new ExplorerDataFetcher(signals, refinement, strategy, new ExplorerMapper());
  }
}
