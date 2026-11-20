package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemRepository;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementDecisionRepository;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.RefinementQuestionRepository;
import lab.wide.domain.Solution;
import lab.wide.domain.SolutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefinementDetailServiceTest extends TestBase {

  @Mock RefinementQuestionRepository refinementQuestionRepository;

  @Mock RefinementDecisionRepository refinementDecisionRepository;

  @Mock ProblemRepository problemRepository;

  @Mock SolutionRepository solutionRepository;

  final Clock clock = fixedClock();

  RefinementDetailService refinementDetailService;

  @BeforeEach
  void createService() {
    refinementDetailService =
        new RefinementDetailService(
            refinementQuestionRepository,
            refinementDecisionRepository,
            problemRepository,
            solutionRepository,
            clock);
  }

  @Test
  void questionsForProblem_problemId_returnsItsQuestionsInCreationOrder() {
    var problemId = UUID.randomUUID();
    var refinementQuestion = question(problemId, null, QuestionStatus.OPEN);
    when(refinementQuestionRepository.findByProblemIdOrderByCreatedAtAscIdAsc(problemId))
        .thenReturn(List.of(refinementQuestion));

    var refinementQuestions = refinementDetailService.questionsForProblem(problemId);

    assertThat(refinementQuestions).containsExactly(refinementQuestion);
  }

  @Test
  void questionsForSolution_solutionId_returnsItsQuestionsInCreationOrder() {
    var solutionId = UUID.randomUUID();
    var refinementQuestion = question(null, solutionId, QuestionStatus.OPEN);
    when(refinementQuestionRepository.findBySolutionIdOrderByCreatedAtAscIdAsc(solutionId))
        .thenReturn(List.of(refinementQuestion));

    var refinementQuestions = refinementDetailService.questionsForSolution(solutionId);

    assertThat(refinementQuestions).containsExactly(refinementQuestion);
  }

  @Test
  void decisionsForProblem_problemId_returnsItsDecisionsInCreationOrder() {
    var problemId = UUID.randomUUID();
    var refinementDecision = decision(problemId, null);
    when(refinementDecisionRepository.findByProblemIdOrderByCreatedAtAscIdAsc(problemId))
        .thenReturn(List.of(refinementDecision));

    var refinementDecisions = refinementDetailService.decisionsForProblem(problemId);

    assertThat(refinementDecisions).containsExactly(refinementDecision);
  }

  @Test
  void decisionsForSolution_solutionId_returnsItsDecisionsInCreationOrder() {
    var solutionId = UUID.randomUUID();
    var refinementDecision = decision(null, solutionId);
    when(refinementDecisionRepository.findBySolutionIdOrderByCreatedAtAscIdAsc(solutionId))
        .thenReturn(List.of(refinementDecision));

    var refinementDecisions = refinementDetailService.decisionsForSolution(solutionId);

    assertThat(refinementDecisions).containsExactly(refinementDecision);
  }

  @Test
  void createQuestion_noStatus_savesAnOpenQuestion() {
    var problem = knownProblem();
    var question = randomString();
    savesQuestions();

    var refinementQuestion =
        refinementDetailService.createQuestion(
            problem.id(), null, question, null, null, null, null);

    assertThat(refinementQuestion.id()).isNull();
    assertThat(refinementQuestion.problemId()).isEqualTo(problem.id());
    assertThat(refinementQuestion.solutionId()).isNull();
    assertThat(refinementQuestion.question()).isEqualTo(question);
    assertThat(refinementQuestion.status()).isEqualTo(QuestionStatus.OPEN);
    assertThat(refinementQuestion.createdAt()).isEqualTo(clock.instant());
    assertThat(refinementQuestion.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void createQuestion_answeredOnASolution_savesTheAnswerAndAttribution() {
    var solution = knownSolution();
    var answer = randomString();
    var context = randomString();
    var source = randomString();
    savesQuestions();

    var refinementQuestion =
        refinementDetailService.createQuestion(
            null, solution.id(), randomString(), QuestionStatus.ANSWERED, answer, context, source);

    assertThat(refinementQuestion.solutionId()).isEqualTo(solution.id());
    assertThat(refinementQuestion.problemId()).isNull();
    assertThat(refinementQuestion.status()).isEqualTo(QuestionStatus.ANSWERED);
    assertThat(refinementQuestion.answer()).isEqualTo(answer);
    assertThat(refinementQuestion.context()).isEqualTo(context);
    assertThat(refinementQuestion.source()).isEqualTo(source);
  }

  @Test
  void createQuestion_problemParent_advancesTheProblemUpdateTime() {
    var problem = knownProblem();
    savesQuestions();

    refinementDetailService.createQuestion(
        problem.id(), null, randomString(), null, null, null, null);

    verify(problemRepository).touch(problem.id(), clock.instant());
  }

  @Test
  void createQuestion_solutionParent_advancesTheSolutionUpdateTime() {
    var solution = knownSolution();
    savesQuestions();

    refinementDetailService.createQuestion(
        null, solution.id(), randomString(), null, null, null, null);

    verify(solutionRepository).touch(solution.id(), clock.instant());
  }

  @Test
  void createQuestion_bothParents_throwsWithoutSaving() {
    assertThatThrownBy(
            () ->
                refinementDetailService.createQuestion(
                    UUID.randomUUID(), UUID.randomUUID(), randomString(), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide exactly one problemId or solutionId.");
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void createQuestion_noParent_throwsWithoutSaving() {
    assertThatThrownBy(
            () ->
                refinementDetailService.createQuestion(
                    null, null, randomString(), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide exactly one problemId or solutionId.");
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void createQuestion_unknownProblem_throwsWithoutSaving() {
    var problemId = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                refinementDetailService.createQuestion(
                    problemId, null, randomString(), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Problem not found: " + problemId);
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void createQuestion_unknownSolution_throwsWithoutSaving() {
    var solutionId = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                refinementDetailService.createQuestion(
                    null, solutionId, randomString(), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + solutionId);
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void createQuestion_nullQuestion_throwsWithoutSaving() {
    var problem = knownProblem();

    assertThatThrownBy(
            () ->
                refinementDetailService.createQuestion(
                    problem.id(), null, null, null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank question is required.");
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void createQuestion_answeredWithoutAnAnswer_throwsWithoutSaving() {
    var problem = knownProblem();

    assertThatThrownBy(
            () ->
                refinementDetailService.createQuestion(
                    problem.id(), null, randomString(), QuestionStatus.ANSWERED, " ", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank answer for an answered question is required.");
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void editQuestion_answeredWithAnAnswer_keepsTheOtherFields() {
    var existing = storedQuestion(QuestionStatus.OPEN);
    var answer = randomString();
    savesQuestions();

    var edited =
        refinementDetailService.editQuestion(
            existing.id(), Map.of("status", "ANSWERED", "answer", answer), null, null, null);

    assertThat(edited)
        .isEqualTo(
            new RefinementQuestion(
                existing.id(),
                existing.problemId(),
                null,
                existing.question(),
                QuestionStatus.ANSWERED,
                answer,
                existing.context(),
                existing.source(),
                existing.createdAt(),
                edited.updatedAt()));
    assertThat(edited.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void editQuestion_reopened_keepsTheEarlierAnswer() {
    var existing = storedQuestion(QuestionStatus.ANSWERED);
    savesQuestions();

    var edited =
        refinementDetailService.editQuestion(
            existing.id(), Map.of("status", "OPEN"), null, null, null);

    assertThat(edited.status()).isEqualTo(QuestionStatus.OPEN);
    assertThat(edited.answer()).isEqualTo(existing.answer());
  }

  @Test
  void editQuestion_answerClearedWhileOpen_savesNoAnswer() {
    var existing = storedQuestion(QuestionStatus.OPEN);
    savesQuestions();

    var edited =
        refinementDetailService.editQuestion(existing.id(), null, List.of("answer"), null, null);

    assertThat(edited.answer()).isNull();
  }

  @Test
  void editQuestion_answerClearedWhileAnswered_throwsWithoutSaving() {
    var existing = storedQuestion(QuestionStatus.ANSWERED);

    assertThatThrownBy(
            () ->
                refinementDetailService.editQuestion(
                    existing.id(), null, List.of("answer"), null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank answer for an answered question is required.");
    verify(refinementQuestionRepository, never()).save(any());
  }

  @Test
  void editQuestion_unknownStatus_throwsWithoutSaving() {
    var existing = storedQuestion(QuestionStatus.OPEN);

    assertThatThrownBy(
            () ->
                refinementDetailService.editQuestion(
                    existing.id(), Map.of("status", "DONE"), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Question status must be OPEN or ANSWERED.");
    verify(refinementQuestionRepository, never()).save(any());
  }

  @Test
  void editQuestion_noChanges_throwsBeforeReading() {
    assertThatThrownBy(
            () -> refinementDetailService.editQuestion(UUID.randomUUID(), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide at least one field to edit or clear.");
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void editQuestion_requiredFieldCleared_throwsBeforeReading() {
    assertThatThrownBy(
            () ->
                refinementDetailService.editQuestion(
                    UUID.randomUUID(), null, List.of("question"), null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot clear required field: question");
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void editQuestion_parentIdAsAField_throwsBeforeReading() {
    var fields = Map.of("problemId", UUID.randomUUID().toString());

    assertThatThrownBy(
            () -> refinementDetailService.editQuestion(UUID.randomUUID(), fields, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("Unknown editable field: problemId");
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void editQuestion_unknownId_throwsNotFound() {
    var id = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                refinementDetailService.editQuestion(
                    id, Map.of("question", randomString()), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Question not found: " + id);
  }

  @Test
  void editQuestion_movedToASolution_keepsIdentityStatusAndAnswer() {
    var existing = storedQuestion(QuestionStatus.ANSWERED);
    var solution = knownSolution();
    savesQuestions();

    var moved =
        refinementDetailService.editQuestion(existing.id(), null, null, null, solution.id());

    assertThat(moved)
        .isEqualTo(
            new RefinementQuestion(
                existing.id(),
                null,
                solution.id(),
                existing.question(),
                existing.status(),
                existing.answer(),
                existing.context(),
                existing.source(),
                existing.createdAt(),
                clock.instant()));
  }

  @Test
  void editQuestion_movedToASolution_advancesBothParents() {
    var existing = storedQuestion(QuestionStatus.OPEN);
    var solution = knownSolution();
    savesQuestions();

    refinementDetailService.editQuestion(existing.id(), null, null, null, solution.id());

    verify(problemRepository).touch(existing.problemId(), clock.instant());
    verify(solutionRepository).touch(solution.id(), clock.instant());
  }

  @Test
  void editQuestion_movedWithEmptyEdits_keepsTheFields() {
    var existing = storedQuestion(QuestionStatus.OPEN);
    var solution = knownSolution();
    savesQuestions();

    var moved =
        refinementDetailService.editQuestion(
            existing.id(), Map.of(), List.of(), null, solution.id());

    assertThat(moved.solutionId()).isEqualTo(solution.id());
    assertThat(moved.question()).isEqualTo(existing.question());
    assertThat(moved.context()).isEqualTo(existing.context());
  }

  @Test
  void editQuestion_movedWithAnEdit_appliesTheEdit() {
    var existing = storedQuestion(QuestionStatus.OPEN);
    var solution = knownSolution();
    var context = randomString();
    savesQuestions();

    var moved =
        refinementDetailService.editQuestion(
            existing.id(), Map.of("context", context), null, null, solution.id());

    assertThat(moved.solutionId()).isEqualTo(solution.id());
    assertThat(moved.context()).isEqualTo(context);
  }

  @Test
  void editQuestion_movedWithAClearedField_clearsIt() {
    var existing = storedQuestion(QuestionStatus.OPEN);
    var solution = knownSolution();
    savesQuestions();

    var moved =
        refinementDetailService.editQuestion(
            existing.id(), null, List.of("source"), null, solution.id());

    assertThat(moved.solutionId()).isEqualTo(solution.id());
    assertThat(moved.source()).isNull();
  }

  @Test
  void editQuestion_movedToTheCurrentParent_advancesItOnce() {
    var existing = storedQuestion(QuestionStatus.OPEN);
    when(problemRepository.existsById(existing.problemId())).thenReturn(true);
    savesQuestions();

    refinementDetailService.editQuestion(existing.id(), null, null, existing.problemId(), null);

    verify(problemRepository).touch(existing.problemId(), clock.instant());
    verifyNoInteractions(solutionRepository);
  }

  @Test
  void editQuestion_movedToBothParents_throwsBeforeReading() {
    assertThatThrownBy(
            () ->
                refinementDetailService.editQuestion(
                    UUID.randomUUID(), null, null, UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide exactly one problemId or solutionId.");
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void editQuestion_movedToAnUnknownSolution_throwsBeforeReading() {
    var solutionId = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                refinementDetailService.editQuestion(
                    UUID.randomUUID(), null, null, null, solutionId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + solutionId);
    verifyNoInteractions(refinementQuestionRepository);
  }

  @Test
  void createDecision_solutionParent_savesThePositionAndAdvancesTheSolution() {
    var solution = knownSolution();
    var decision = randomString();
    var rationale = randomString();
    var source = randomString();
    var revisitWhen = randomString();
    savesDecisions();

    var refinementDecision =
        refinementDetailService.createDecision(
            null, solution.id(), decision, rationale, source, revisitWhen);

    verify(solutionRepository).touch(solution.id(), clock.instant());
    assertThat(refinementDecision)
        .isEqualTo(
            new RefinementDecision(
                null,
                null,
                solution.id(),
                decision,
                rationale,
                source,
                revisitWhen,
                clock.instant(),
                clock.instant()));
  }

  @Test
  void createDecision_problemParent_advancesTheProblem() {
    var problem = knownProblem();
    savesDecisions();

    refinementDetailService.createDecision(problem.id(), null, randomString(), null, null, null);

    verify(problemRepository).touch(problem.id(), clock.instant());
  }

  @Test
  void createDecision_blankDecision_throwsWithoutSaving() {
    var problem = knownProblem();

    assertThatThrownBy(
            () -> refinementDetailService.createDecision(problem.id(), null, " ", null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank decision is required.");
    verifyNoInteractions(refinementDecisionRepository);
  }

  @Test
  void createDecision_noParent_throwsWithoutSaving() {
    assertThatThrownBy(
            () ->
                refinementDetailService.createDecision(
                    null, null, randomString(), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide exactly one problemId or solutionId.");
    verifyNoInteractions(refinementDecisionRepository);
  }

  @Test
  void editDecision_newRevisitCondition_keepsOmittedFieldsAndParent() {
    var existing = storedDecision();
    var revisitWhen = randomString();
    savesDecisions();

    var edited =
        refinementDetailService.editDecision(
            existing.id(), Map.of("revisitWhen", revisitWhen), null);

    assertThat(edited)
        .isEqualTo(
            new RefinementDecision(
                existing.id(),
                null,
                existing.solutionId(),
                existing.decision(),
                existing.rationale(),
                existing.source(),
                revisitWhen,
                existing.createdAt(),
                edited.updatedAt()));
    assertThat(edited.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void editDecision_validEdit_advancesTheParent() {
    var existing = storedDecision();
    savesDecisions();

    refinementDetailService.editDecision(existing.id(), Map.of("decision", randomString()), null);

    verify(solutionRepository).touch(existing.solutionId(), clock.instant());
  }

  @Test
  void editDecision_clearedOptionalFields_savesThemEmpty() {
    var existing = storedDecision();
    savesDecisions();

    var edited =
        refinementDetailService.editDecision(
            existing.id(), null, List.of("rationale", "source", "revisitWhen"));

    assertThat(edited.rationale()).isNull();
    assertThat(edited.source()).isNull();
    assertThat(edited.revisitWhen()).isNull();
  }

  @Test
  void editDecision_emptyEdit_throwsBeforeReading() {
    assertThatThrownBy(
            () -> refinementDetailService.editDecision(UUID.randomUUID(), Map.of(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide at least one field to edit or clear.");
    verifyNoInteractions(refinementDecisionRepository);
  }

  @Test
  void editDecision_blankDecision_throwsWithoutSaving() {
    var existing = storedDecision();

    assertThatThrownBy(
            () ->
                refinementDetailService.editDecision(existing.id(), Map.of("decision", " "), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank decision is required.");
    verify(refinementDecisionRepository, never()).save(any());
  }

  @Test
  void editDecision_unknownId_throwsNotFound() {
    var id = UUID.randomUUID();

    assertThatThrownBy(
            () ->
                refinementDetailService.editDecision(id, Map.of("decision", randomString()), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Decision not found: " + id);
  }

  private void savesQuestions() {
    when(refinementQuestionRepository.save(any(RefinementQuestion.class)))
        .thenAnswer(returnsFirstArg());
  }

  private void savesDecisions() {
    when(refinementDecisionRepository.save(any(RefinementDecision.class)))
        .thenAnswer(returnsFirstArg());
  }

  private Problem knownProblem() {
    var problem = problem(UUID.randomUUID());
    when(problemRepository.existsById(problem.id())).thenReturn(true);
    return problem;
  }

  private Solution knownSolution() {
    var solution = solution(UUID.randomUUID());
    when(solutionRepository.existsById(solution.id())).thenReturn(true);
    return solution;
  }

  private RefinementQuestion storedQuestion(QuestionStatus status) {
    var refinementQuestion = question(UUID.randomUUID(), null, status);
    when(refinementQuestionRepository.findById(refinementQuestion.id()))
        .thenReturn(Optional.of(refinementQuestion));
    return refinementQuestion;
  }

  private RefinementDecision storedDecision() {
    var refinementDecision = decision(null, UUID.randomUUID());
    when(refinementDecisionRepository.findById(refinementDecision.id()))
        .thenReturn(Optional.of(refinementDecision));
    return refinementDecision;
  }

  private Problem problem(UUID id) {
    var createdAt = clock.instant().minusSeconds(60);
    return new Problem(
        id, randomString(), randomString(), randomString(), randomString(), createdAt, createdAt);
  }

  private Solution solution(UUID id) {
    var createdAt = clock.instant().minusSeconds(60);
    return new Solution(
        id,
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        createdAt,
        createdAt);
  }

  private RefinementQuestion question(UUID problemId, UUID solutionId, QuestionStatus status) {
    var createdAt = clock.instant().minusSeconds(30);
    return new RefinementQuestion(
        UUID.randomUUID(),
        problemId,
        solutionId,
        randomString(),
        status,
        randomString(),
        randomString(),
        randomString(),
        createdAt,
        createdAt);
  }

  private RefinementDecision decision(UUID problemId, UUID solutionId) {
    var createdAt = clock.instant().minusSeconds(30);
    return new RefinementDecision(
        UUID.randomUUID(),
        problemId,
        solutionId,
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        createdAt,
        createdAt);
  }
}
