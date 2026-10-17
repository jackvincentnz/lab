package lab.wide.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.domain.Problem;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Solution;
import lab.wide.infrastructure.ProblemRepository;
import lab.wide.infrastructure.RefinementDecisionRepository;
import lab.wide.infrastructure.RefinementQuestionRepository;
import lab.wide.infrastructure.SolutionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefinementDetailServiceTest extends TestBase {

  @Mock RefinementQuestionRepository questions;

  @Mock RefinementDecisionRepository decisions;

  @Mock ProblemRepository problems;

  @Mock SolutionRepository solutions;

  @InjectMocks RefinementDetailService service;

  @Captor ArgumentCaptor<Problem> problemCaptor;

  @Captor ArgumentCaptor<Solution> solutionCaptor;

  @Test
  void createQuestion_defaultsToOpen() {
    var problem = touchable(knownProblem());
    var question = randomString();
    savesQuestions();

    var saved = service.createQuestion(problem.id(), null, question, null, null, null, null);

    assertThat(saved.problemId()).isEqualTo(problem.id());
    assertThat(saved.solutionId()).isNull();
    assertThat(saved.question()).isEqualTo(question);
    assertThat(saved.status()).isEqualTo(QuestionStatus.OPEN);
    assertThat(saved.createdAt()).isNotNull().isEqualTo(saved.updatedAt());
  }

  @Test
  void createQuestion_advancesOnlyTheParentUpdateTime() {
    var problem = touchable(knownProblem());
    savesQuestions();

    var saved = service.createQuestion(problem.id(), null, randomString(), null, null, null, null);

    verify(problems).save(problemCaptor.capture());
    var touched = problemCaptor.getValue();
    assertThat(touched.updatedAt()).isEqualTo(saved.updatedAt());
    assertThat(touched.createdAt()).isEqualTo(problem.createdAt());
    assertThat(touched.description()).isEqualTo(problem.description());
  }

  @Test
  void createQuestion_rejectsBothParents() {
    assertThatThrownBy(
            () ->
                service.createQuestion(
                    UUID.randomUUID(), UUID.randomUUID(), randomString(), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide exactly one problemId or solutionId.");
    verifyNoInteractions(questions);
  }

  @Test
  void createQuestion_rejectsMissingParent() {
    assertThatThrownBy(
            () -> service.createQuestion(null, null, randomString(), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide exactly one problemId or solutionId.");
    verifyNoInteractions(questions);
  }

  @Test
  void createQuestion_rejectsUnknownSolution() {
    var solutionId = UUID.randomUUID();
    when(solutions.existsById(solutionId)).thenReturn(false);

    assertThatThrownBy(
            () -> service.createQuestion(null, solutionId, randomString(), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + solutionId);
    verifyNoInteractions(questions);
  }

  @Test
  void createQuestion_rejectsAnsweredStatusWithoutAnAnswer() {
    var problem = knownProblem();

    assertThatThrownBy(
            () ->
                service.createQuestion(
                    problem.id(), null, randomString(), QuestionStatus.ANSWERED, " ", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("answer");
    verifyNoInteractions(questions);
  }

  @Test
  void editQuestion_answersWhilePreservingOtherFields() {
    var existing = question(QuestionStatus.OPEN);
    var answer = randomString();
    savesQuestions();

    var edited =
        service.editQuestion(
            existing.id(), Map.of("status", "ANSWERED", "answer", answer), null, null, null);

    assertThat(edited.status()).isEqualTo(QuestionStatus.ANSWERED);
    assertThat(edited.answer()).isEqualTo(answer);
    assertThat(edited.question()).isEqualTo(existing.question());
    assertThat(edited.context()).isEqualTo(existing.context());
    assertThat(edited.source()).isEqualTo(existing.source());
    assertThat(edited.createdAt()).isEqualTo(existing.createdAt());
    assertThat(edited.updatedAt()).isAfter(existing.updatedAt());
  }

  @Test
  void editQuestion_reopeningKeepsTheEarlierAnswer() {
    var existing = question(QuestionStatus.ANSWERED);
    savesQuestions();

    var edited = service.editQuestion(existing.id(), Map.of("status", "OPEN"), null, null, null);

    assertThat(edited.status()).isEqualTo(QuestionStatus.OPEN);
    assertThat(edited.answer()).isEqualTo(existing.answer());
  }

  @Test
  void editQuestion_clearsTheAnswerOfAnOpenQuestion() {
    var existing = question(QuestionStatus.OPEN);
    savesQuestions();

    var edited = service.editQuestion(existing.id(), null, List.of("answer"), null, null);

    assertThat(edited.answer()).isNull();
  }

  @Test
  void editQuestion_rejectsClearingTheAnswerWhileAnswered() {
    var existing = storedQuestion(QuestionStatus.ANSWERED);

    assertThatThrownBy(
            () -> service.editQuestion(existing.id(), null, List.of("answer"), null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("answer");
    verify(questions, never()).save(any());
  }

  @Test
  void editQuestion_rejectsUnknownStatus() {
    var existing = storedQuestion(QuestionStatus.OPEN);

    assertThatThrownBy(
            () -> service.editQuestion(existing.id(), Map.of("status", "DONE"), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Question status must be OPEN or ANSWERED.");
    verify(questions, never()).save(any());
  }

  @Test
  void editQuestion_rejectsClearingTheQuestionText() {
    assertThatThrownBy(
            () ->
                service.editQuestion(
                    UUID.randomUUID(),
                    Map.of("question", randomString()),
                    List.of("question"),
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cannot clear required field: question");
    verifyNoInteractions(questions);
  }

  @Test
  void editQuestion_rejectsParentIdsSuppliedAsFields() {
    assertThatThrownBy(
            () ->
                service.editQuestion(
                    UUID.randomUUID(),
                    Map.of("problemId", UUID.randomUUID().toString()),
                    null,
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("Unknown editable field: problemId");
    verifyNoInteractions(questions);
  }

  @Test
  void editQuestion_movesToASolutionAndTouchesBothParents() {
    var existing = question(QuestionStatus.ANSWERED);
    var solution = touchable(knownSolution());
    savesQuestions();

    var moved = service.editQuestion(existing.id(), null, null, null, solution.id());

    assertThat(moved.id()).isEqualTo(existing.id());
    assertThat(moved.problemId()).isNull();
    assertThat(moved.solutionId()).isEqualTo(solution.id());
    assertThat(moved.status()).isEqualTo(existing.status());
    assertThat(moved.answer()).isEqualTo(existing.answer());
    assertThat(moved.createdAt()).isEqualTo(existing.createdAt());
    verify(problems).save(problemCaptor.capture());
    assertThat(problemCaptor.getValue().id()).isEqualTo(existing.problemId());
    verify(solutions).save(solutionCaptor.capture());
    assertThat(solutionCaptor.getValue().id()).isEqualTo(solution.id());
  }

  @Test
  void editQuestion_rejectsAMoveNamingBothParents() {
    assertThatThrownBy(
            () ->
                service.editQuestion(
                    UUID.randomUUID(), null, null, UUID.randomUUID(), UUID.randomUUID()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide exactly one problemId or solutionId.");
    verifyNoInteractions(questions);
  }

  @Test
  void editQuestion_rejectsUnknownQuestion() {
    var id = UUID.randomUUID();
    when(questions.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.editQuestion(id, Map.of("question", randomString()), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Question not found: " + id);
  }

  @Test
  void createDecision_savesThePositionAndTouchesTheSolution() {
    var solution = touchable(knownSolution());
    var decision = randomString();
    var source = randomString();
    savesDecisions();

    var saved = service.createDecision(null, solution.id(), decision, null, source, null);

    assertThat(saved.solutionId()).isEqualTo(solution.id());
    assertThat(saved.decision()).isEqualTo(decision);
    assertThat(saved.source()).isEqualTo(source);
    verify(solutions).save(solutionCaptor.capture());
    assertThat(solutionCaptor.getValue().updatedAt()).isEqualTo(saved.updatedAt());
  }

  @Test
  void createDecision_rejectsBlankDecision() {
    var problem = knownProblem();

    assertThatThrownBy(() -> service.createDecision(problem.id(), null, " ", null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank decision is required.");
    verifyNoInteractions(decisions);
  }

  @Test
  void editDecision_keepsOmittedFieldsAndParent() {
    var existing = decision();
    var revisitWhen = randomString();
    savesDecisions();

    var edited = service.editDecision(existing.id(), Map.of("revisitWhen", revisitWhen), null);

    assertThat(edited.id()).isEqualTo(existing.id());
    assertThat(edited.solutionId()).isEqualTo(existing.solutionId());
    assertThat(edited.decision()).isEqualTo(existing.decision());
    assertThat(edited.rationale()).isEqualTo(existing.rationale());
    assertThat(edited.revisitWhen()).isEqualTo(revisitWhen);
    assertThat(edited.createdAt()).isEqualTo(existing.createdAt());
  }

  @Test
  void editDecision_clearsOptionalFields() {
    var existing = decision();
    savesDecisions();

    var edited = service.editDecision(existing.id(), null, List.of("rationale", "source"));

    assertThat(edited.rationale()).isNull();
    assertThat(edited.source()).isNull();
  }

  @Test
  void editDecision_rejectsAnEmptyEdit() {
    assertThatThrownBy(() -> service.editDecision(UUID.randomUUID(), Map.of(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Provide at least one field to edit or clear.");
    verifyNoInteractions(decisions);
  }

  @Test
  void editDecision_rejectsBlankDecisionWithoutSaving() {
    var existing = storedDecision();

    assertThatThrownBy(() -> service.editDecision(existing.id(), Map.of("decision", " "), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank decision is required.");
    verify(decisions, never()).save(any());
  }

  @Test
  void editDecision_rejectsUnknownDecision() {
    var id = UUID.randomUUID();
    when(decisions.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.editDecision(id, Map.of("decision", randomString()), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Decision not found: " + id);
  }

  private void savesQuestions() {
    when(questions.save(any(RefinementQuestion.class))).thenAnswer(call -> call.getArgument(0));
  }

  private void savesDecisions() {
    when(decisions.save(any(RefinementDecision.class))).thenAnswer(call -> call.getArgument(0));
  }

  private Problem problem(UUID id) {
    var created = Instant.now().minusSeconds(60);
    return new Problem(id, randomString(), randomString(), null, null, created, created);
  }

  private Problem knownProblem() {
    var problem = problem(UUID.randomUUID());
    when(problems.existsById(problem.id())).thenReturn(true);
    return problem;
  }

  private Problem touchable(Problem problem) {
    when(problems.findById(problem.id())).thenReturn(Optional.of(problem));
    return problem;
  }

  private Solution knownSolution() {
    var created = Instant.now().minusSeconds(60);
    var solution =
        new Solution(
            UUID.randomUUID(), randomString(), randomString(), null, null, null, created, created);
    when(solutions.existsById(solution.id())).thenReturn(true);
    return solution;
  }

  private Solution touchable(Solution solution) {
    when(solutions.findById(solution.id())).thenReturn(Optional.of(solution));
    return solution;
  }

  // A question whose problem can be touched after a successful edit.
  private RefinementQuestion question(QuestionStatus status) {
    var question = storedQuestion(status);
    touchable(problem(question.problemId()));
    return question;
  }

  private RefinementQuestion storedQuestion(QuestionStatus status) {
    var created = Instant.now().minusSeconds(30);
    var question =
        new RefinementQuestion(
            UUID.randomUUID(),
            UUID.randomUUID(),
            null,
            randomString(),
            status,
            randomString(),
            randomString(),
            randomString(),
            created,
            created);
    when(questions.findById(question.id())).thenReturn(Optional.of(question));
    return question;
  }

  // A decision whose solution can be touched after a successful edit.
  private RefinementDecision decision() {
    var decision = storedDecision();
    var created = Instant.now().minusSeconds(60);
    touchable(
        new Solution(
            decision.solutionId(),
            randomString(),
            randomString(),
            null,
            null,
            null,
            created,
            created));
    return decision;
  }

  private RefinementDecision storedDecision() {
    var created = Instant.now().minusSeconds(30);
    var decision =
        new RefinementDecision(
            UUID.randomUUID(),
            null,
            UUID.randomUUID(),
            randomString(),
            randomString(),
            randomString(),
            randomString(),
            created,
            created);
    when(decisions.findById(decision.id())).thenReturn(Optional.of(decision));
    return decision;
  }
}
