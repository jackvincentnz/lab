package lab.wide.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.api.gql.types.QuestionStatus;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Signal;
import lab.wide.domain.Solution;
import lab.wide.domain.Strategy;
import org.junit.jupiter.api.Test;

class ExplorerMapperTest extends TestBase {

  private final ExplorerMapper explorerMapper = new ExplorerMapper();

  @Test
  void map_signal_writesIdAndCaptureTimeAsStrings() {
    var signal =
        new Signal(
            UUID.randomUUID(), randomString(), randomString(), randomString(), Instant.now());

    var mapped = explorerMapper.map(signal);

    assertThat(mapped.getId()).isEqualTo(signal.id().toString());
    assertThat(mapped.getTitle()).isEqualTo(signal.title());
    assertThat(mapped.getContent()).isEqualTo(signal.content());
    assertThat(mapped.getSource()).isEqualTo(signal.source());
    assertThat(mapped.getCapturedAt()).isEqualTo(signal.capturedAt().toString());
  }

  @Test
  void map_problem_leavesNestedListsForFieldResolvers() {
    var problem =
        new Problem(
            UUID.randomUUID(),
            randomString(),
            randomString(),
            null,
            randomString(),
            Instant.now(),
            Instant.now());

    var mapped = explorerMapper.map(problem);

    assertThat(mapped.getId()).isEqualTo(problem.id().toString());
    assertThat(mapped.getImpact()).isNull();
    assertThat(mapped.getDesiredOutcome()).isEqualTo(problem.desiredOutcome());
    assertThat(mapped.getCreatedAt()).isEqualTo(problem.createdAt().toString());
    assertThat(mapped.getUpdatedAt()).isEqualTo(problem.updatedAt().toString());
    assertThat(mapped.getQuestions()).isNull();
    assertThat(mapped.getSignals()).isNull();
  }

  @Test
  void map_solution_copiesEveryScalarField() {
    var solution =
        new Solution(
            UUID.randomUUID(),
            randomString(),
            randomString(),
            randomString(),
            randomString(),
            randomString(),
            Instant.now(),
            Instant.now());

    var mapped = explorerMapper.map(solution);

    assertThat(mapped.getApproach()).isEqualTo(solution.approach());
    assertThat(mapped.getScope()).isEqualTo(solution.scope());
    assertThat(mapped.getTradeoffs()).isEqualTo(solution.tradeoffs());
    assertThat(mapped.getEffort()).isEqualTo(solution.effort());
  }

  @Test
  void map_unsetStrategy_keepsNullContentAndTime() {
    var strategy = new Strategy(UUID.randomUUID(), null, null);

    var mapped = explorerMapper.map(strategy);

    assertThat(mapped.getId()).isEqualTo(strategy.id().toString());
    assertThat(mapped.getContent()).isNull();
    assertThat(mapped.getUpdatedAt()).isNull();
  }

  @Test
  void map_problemSignal_exposesRelationshipIdRationaleAndLinkTime() {
    var problemSignal =
        new ProblemSignal(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), randomString(), Instant.now());

    var mapped = explorerMapper.map(problemSignal);

    assertThat(mapped.getId()).isEqualTo(problemSignal.id().toString());
    assertThat(mapped.getRationale()).isEqualTo(problemSignal.rationale());
    assertThat(mapped.getLinkedAt()).isEqualTo(problemSignal.linkedAt().toString());
    assertThat(mapped.getProblem()).isNull();
    assertThat(mapped.getSignal()).isNull();
  }

  @Test
  void map_refinementQuestion_translatesStatusAndParentIds() {
    var refinementQuestion =
        new RefinementQuestion(
            UUID.randomUUID(),
            null,
            UUID.randomUUID(),
            randomString(),
            lab.wide.domain.QuestionStatus.ANSWERED,
            randomString(),
            randomString(),
            randomString(),
            Instant.now(),
            Instant.now());

    var mapped = explorerMapper.map(refinementQuestion);

    assertThat(mapped.getProblemId()).isNull();
    assertThat(mapped.getSolutionId()).isEqualTo(refinementQuestion.solutionId().toString());
    assertThat(mapped.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    assertThat(mapped.getAnswer()).isEqualTo(refinementQuestion.answer());
    assertThat(mapped.getContext()).isEqualTo(refinementQuestion.context());
    assertThat(mapped.getSource()).isEqualTo(refinementQuestion.source());
  }

  @Test
  void map_refinementDecision_copiesRationaleSourceAndRevisitCondition() {
    var refinementDecision =
        new RefinementDecision(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            randomString(),
            randomString(),
            randomString(),
            randomString(),
            Instant.now(),
            Instant.now());

    var mapped = explorerMapper.map(refinementDecision);

    assertThat(mapped.getProblemId()).isEqualTo(refinementDecision.problemId().toString());
    assertThat(mapped.getSolutionId()).isNull();
    assertThat(mapped.getDecision()).isEqualTo(refinementDecision.decision());
    assertThat(mapped.getRationale()).isEqualTo(refinementDecision.rationale());
    assertThat(mapped.getSource()).isEqualTo(refinementDecision.source());
    assertThat(mapped.getRevisitWhen()).isEqualTo(refinementDecision.revisitWhen());
  }
}
