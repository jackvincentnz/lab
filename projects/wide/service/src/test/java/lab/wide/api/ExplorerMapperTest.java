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

  private final ExplorerMapper mapper = new ExplorerMapper();

  @Test
  void map_signalWritesIdAndCaptureTimeAsStrings() {
    var signal =
        new Signal(
            UUID.randomUUID(), randomString(), randomString(), randomString(), Instant.now());

    var mapped = mapper.map(signal);

    assertThat(mapped.getId()).isEqualTo(signal.id().toString());
    assertThat(mapped.getTitle()).isEqualTo(signal.title());
    assertThat(mapped.getContent()).isEqualTo(signal.content());
    assertThat(mapped.getSource()).isEqualTo(signal.source());
    assertThat(mapped.getCapturedAt()).isEqualTo(signal.capturedAt().toString());
  }

  @Test
  void map_problemLeavesNestedListsForFieldResolvers() {
    var problem =
        new Problem(
            UUID.randomUUID(),
            randomString(),
            randomString(),
            null,
            randomString(),
            Instant.now(),
            Instant.now());

    var mapped = mapper.map(problem);

    assertThat(mapped.getId()).isEqualTo(problem.id().toString());
    assertThat(mapped.getImpact()).isNull();
    assertThat(mapped.getDesiredOutcome()).isEqualTo(problem.desiredOutcome());
    assertThat(mapped.getCreatedAt()).isEqualTo(problem.createdAt().toString());
    assertThat(mapped.getUpdatedAt()).isEqualTo(problem.updatedAt().toString());
    assertThat(mapped.getQuestions()).isNull();
    assertThat(mapped.getSignals()).isNull();
  }

  @Test
  void map_solutionCopiesEveryScalarField() {
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

    var mapped = mapper.map(solution);

    assertThat(mapped.getApproach()).isEqualTo(solution.approach());
    assertThat(mapped.getScope()).isEqualTo(solution.scope());
    assertThat(mapped.getTradeoffs()).isEqualTo(solution.tradeoffs());
    assertThat(mapped.getEffort()).isEqualTo(solution.effort());
  }

  @Test
  void map_unsetStrategyKeepsNullContentAndTime() {
    var strategy = new Strategy(UUID.randomUUID(), null, null);

    var mapped = mapper.map(strategy);

    assertThat(mapped.getId()).isEqualTo(strategy.id().toString());
    assertThat(mapped.getContent()).isNull();
    assertThat(mapped.getUpdatedAt()).isNull();
  }

  @Test
  void map_linkExposesRelationshipIdRationaleAndLinkTime() {
    var link =
        new ProblemSignal(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), randomString(), Instant.now());

    var mapped = mapper.map(link);

    assertThat(mapped.getId()).isEqualTo(link.id().toString());
    assertThat(mapped.getRationale()).isEqualTo(link.rationale());
    assertThat(mapped.getLinkedAt()).isEqualTo(link.linkedAt().toString());
    assertThat(mapped.getProblem()).isNull();
    assertThat(mapped.getSignal()).isNull();
  }

  @Test
  void map_questionTranslatesStatusAndParentIds() {
    var question =
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

    var mapped = mapper.map(question);

    assertThat(mapped.getProblemId()).isNull();
    assertThat(mapped.getSolutionId()).isEqualTo(question.solutionId().toString());
    assertThat(mapped.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    assertThat(mapped.getAnswer()).isEqualTo(question.answer());
    assertThat(mapped.getContext()).isEqualTo(question.context());
    assertThat(mapped.getSource()).isEqualTo(question.source());
  }

  @Test
  void map_decisionCopiesRationaleSourceAndRevisitCondition() {
    var decision =
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

    var mapped = mapper.map(decision);

    assertThat(mapped.getProblemId()).isEqualTo(decision.problemId().toString());
    assertThat(mapped.getSolutionId()).isNull();
    assertThat(mapped.getDecision()).isEqualTo(decision.decision());
    assertThat(mapped.getRationale()).isEqualTo(decision.rationale());
    assertThat(mapped.getSource()).isEqualTo(decision.source());
    assertThat(mapped.getRevisitWhen()).isEqualTo(decision.revisitWhen());
  }
}
