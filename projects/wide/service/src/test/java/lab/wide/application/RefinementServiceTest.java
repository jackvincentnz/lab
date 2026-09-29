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
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Signal;
import lab.wide.domain.Solution;
import lab.wide.domain.SolutionProblem;
import lab.wide.domain.SolutionSignal;
import lab.wide.infrastructure.ProblemRepository;
import lab.wide.infrastructure.ProblemSignalRepository;
import lab.wide.infrastructure.SignalRepository;
import lab.wide.infrastructure.SolutionProblemRepository;
import lab.wide.infrastructure.SolutionRepository;
import lab.wide.infrastructure.SolutionSignalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class RefinementServiceTest extends TestBase {

  @Mock ProblemRepository problems;

  @Mock SolutionRepository solutions;

  @Mock SignalRepository signals;

  @Mock ProblemSignalRepository problemSignals;

  @Mock SolutionSignalRepository solutionSignals;

  @Mock SolutionProblemRepository solutionProblems;

  @Mock RefinementDetailService details;

  @InjectMocks RefinementService service;

  @Captor ArgumentCaptor<Problem> problemCaptor;

  @Captor ArgumentCaptor<Solution> solutionCaptor;

  @Captor ArgumentCaptor<ProblemSignal> problemSignalCaptor;

  @Captor ArgumentCaptor<SolutionSignal> solutionSignalCaptor;

  @Captor ArgumentCaptor<SolutionProblem> solutionProblemCaptor;

  @Captor ArgumentCaptor<Pageable> pageableCaptor;

  @Test
  void createProblem_savesANewRecordWithMatchingTimestamps() {
    var title = randomString();
    var description = randomString();
    savesProblems();

    var saved = service.createProblem(title, description, null, null);

    verify(problems).save(problemCaptor.capture());
    assertThat(problemCaptor.getValue().id()).isNull();
    assertThat(saved.title()).isEqualTo(title);
    assertThat(saved.description()).isEqualTo(description);
    assertThat(saved.createdAt()).isNotNull().isEqualTo(saved.updatedAt());
  }

  @Test
  void createProblem_rejectsBlankDescription() {
    assertThatThrownBy(() -> service.createProblem(randomString(), " ", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank description is required.");
    verifyNoInteractions(problems);
  }

  @Test
  void createProblem_linksSuppliedSignalsToTheDraft() {
    var problem = savedProblem();
    var signalId = existingSignal();
    var rationale = randomString();
    when(problems.save(any(Problem.class))).thenReturn(problem);
    when(problems.findById(problem.id())).thenReturn(Optional.of(problem));
    when(problemSignals.findByProblemIdAndSignalId(problem.id(), signalId))
        .thenReturn(Optional.empty());

    service.createProblem(
        randomString(), randomString(), null, null, List.of(new SignalLink(signalId, rationale)));

    verify(problemSignals).save(problemSignalCaptor.capture());
    assertThat(problemSignalCaptor.getValue().problemId()).isEqualTo(problem.id());
    assertThat(problemSignalCaptor.getValue().signalId()).isEqualTo(signalId);
    assertThat(problemSignalCaptor.getValue().rationale()).isEqualTo(rationale);
  }

  @Test
  void createProblem_rejectsALinkWithoutASignalId() {
    when(problems.save(any(Problem.class))).thenReturn(savedProblem());

    assertThatThrownBy(
            () ->
                service.createProblem(
                    randomString(),
                    randomString(),
                    null,
                    null,
                    List.of(new SignalLink(null, randomString()))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Each signal link requires a signalId and rationale.");
    verifyNoInteractions(problemSignals);
  }

  @Test
  void createSolution_rejectsBlankTitle() {
    assertThatThrownBy(() -> service.createSolution(null, randomString(), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verifyNoInteractions(solutions);
  }

  @Test
  void createSolution_linksSuppliedSignalsAndProblems() {
    var solution = savedSolution();
    var problem = savedProblem();
    var signalId = existingSignal();
    when(solutions.save(any(Solution.class))).thenReturn(solution);
    when(solutions.findById(solution.id())).thenReturn(Optional.of(solution));
    when(problems.findById(problem.id())).thenReturn(Optional.of(problem));
    when(solutionSignals.findBySolutionIdAndSignalId(solution.id(), signalId))
        .thenReturn(Optional.empty());
    when(solutionProblems.findBySolutionIdAndProblemId(solution.id(), problem.id()))
        .thenReturn(Optional.empty());

    service.createSolution(
        randomString(),
        randomString(),
        null,
        null,
        null,
        List.of(new SignalLink(signalId, randomString())),
        List.of(new ProblemLink(problem.id(), randomString())));

    verify(solutionSignals).save(solutionSignalCaptor.capture());
    assertThat(solutionSignalCaptor.getValue().signalId()).isEqualTo(signalId);
    verify(solutionProblems).save(solutionProblemCaptor.capture());
    assertThat(solutionProblemCaptor.getValue().problemId()).isEqualTo(problem.id());
  }

  @Test
  void createSolution_rejectsALinkWithoutAProblemId() {
    when(solutions.save(any(Solution.class))).thenReturn(savedSolution());

    assertThatThrownBy(
            () ->
                service.createSolution(
                    randomString(),
                    randomString(),
                    null,
                    null,
                    null,
                    null,
                    List.of(new ProblemLink(null, randomString()))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Each problem link requires a problemId and rationale.");
    verifyNoInteractions(solutionProblems);
  }

  @Test
  void editProblem_keepsOmittedFieldsIdentityAndCreationTime() {
    var existing = savedProblem();
    var title = randomString();
    when(problems.findById(existing.id())).thenReturn(Optional.of(existing));
    savesProblems();

    var edited = service.editProblem(existing.id(), Map.of("title", title), null);

    assertThat(edited.id()).isEqualTo(existing.id());
    assertThat(edited.title()).isEqualTo(title);
    assertThat(edited.description()).isEqualTo(existing.description());
    assertThat(edited.impact()).isEqualTo(existing.impact());
    assertThat(edited.createdAt()).isEqualTo(existing.createdAt());
    assertThat(edited.updatedAt()).isAfter(existing.updatedAt());
  }

  @Test
  void editProblem_clearsOptionalFields() {
    var existing = savedProblem();
    when(problems.findById(existing.id())).thenReturn(Optional.of(existing));
    savesProblems();

    var edited = service.editProblem(existing.id(), null, List.of("impact", "desiredOutcome"));

    assertThat(edited.impact()).isNull();
    assertThat(edited.desiredOutcome()).isNull();
  }

  @Test
  void editProblem_rejectsBlankTitleWithoutSaving() {
    var existing = savedProblem();
    when(problems.findById(existing.id())).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.editProblem(existing.id(), Map.of("title", " "), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verify(problems, never()).save(any());
  }

  @Test
  void editProblem_rejectsUnknownFieldBeforeReading() {
    assertThatThrownBy(
            () -> service.editProblem(UUID.randomUUID(), Map.of("typo", randomString()), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("Unknown editable field: typo");
    verifyNoInteractions(problems);
  }

  @Test
  void editProblem_rejectsUnknownProblem() {
    var id = UUID.randomUUID();
    when(problems.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.editProblem(id, Map.of("title", randomString()), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Problem not found: " + id);
  }

  @Test
  void editSolution_clearsOptionalFieldsAndKeepsTheRest() {
    var existing = savedSolution();
    var approach = randomString();
    when(solutions.findById(existing.id())).thenReturn(Optional.of(existing));
    when(solutions.save(any(Solution.class))).thenAnswer(call -> call.getArgument(0));

    var edited =
        service.editSolution(
            existing.id(), Map.of("approach", approach), List.of("scope", "tradeoffs", "effort"));

    assertThat(edited.title()).isEqualTo(existing.title());
    assertThat(edited.approach()).isEqualTo(approach);
    assertThat(edited.scope()).isNull();
    assertThat(edited.tradeoffs()).isNull();
    assertThat(edited.effort()).isNull();
    assertThat(edited.createdAt()).isEqualTo(existing.createdAt());
  }

  @Test
  void editSolution_rejectsBlankApproachWithoutSaving() {
    var existing = savedSolution();
    when(solutions.findById(existing.id())).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.editSolution(existing.id(), Map.of("approach", " "), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank approach is required.");
    verify(solutions, never()).save(any());
  }

  @Test
  void listProblems_pagesMostRecentlyUpdatedFirst() {
    var problem = savedProblem();
    when(problems.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(problem)));

    var listed = service.listProblems(10, 1);

    assertThat(listed).containsExactly(problem);
    verify(problems).findAll(pageableCaptor.capture());
    assertThat(pageableCaptor.getValue())
        .isEqualTo(PageRequest.of(1, 10, Sort.by("updatedAt", "id").descending()));
  }

  @Test
  void listSolutions_defaultsToFiftyPerPage() {
    when(solutions.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

    service.listSolutions(null, null);

    verify(solutions).findAll(pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(50);
    assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
  }

  @Test
  void listProblems_rejectsLimitOverOneHundred() {
    assertThatThrownBy(() -> service.listProblems(101, 0))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(problems);
  }

  @Test
  void listSolutions_rejectsNegativePage() {
    assertThatThrownBy(() -> service.listSolutions(1, -1))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(solutions);
  }

  @Test
  void linkProblemSignal_createsALinkWithItsOwnLinkTime() {
    var problem = savedProblem();
    var signalId = existingSignal();
    var rationale = randomString();
    when(problems.findById(problem.id())).thenReturn(Optional.of(problem));
    when(problemSignals.findByProblemIdAndSignalId(problem.id(), signalId))
        .thenReturn(Optional.empty());
    when(problemSignals.save(any(ProblemSignal.class))).thenAnswer(call -> call.getArgument(0));

    var link = service.linkProblemSignal(problem.id(), signalId, rationale);

    assertThat(link.id()).isNull();
    assertThat(link.rationale()).isEqualTo(rationale);
    assertThat(link.linkedAt()).isNotNull();
  }

  @Test
  void linkProblemSignal_replacesTheRationaleKeepingIdentityAndLinkTime() {
    var problem = savedProblem();
    var signalId = existingSignal();
    var existing =
        new ProblemSignal(UUID.randomUUID(), problem.id(), signalId, randomString(), Instant.now());
    var rationale = randomString();
    when(problems.findById(problem.id())).thenReturn(Optional.of(problem));
    when(problemSignals.findByProblemIdAndSignalId(problem.id(), signalId))
        .thenReturn(Optional.of(existing));
    when(problemSignals.save(any(ProblemSignal.class))).thenAnswer(call -> call.getArgument(0));

    var link = service.linkProblemSignal(problem.id(), signalId, rationale);

    assertThat(link.id()).isEqualTo(existing.id());
    assertThat(link.linkedAt()).isEqualTo(existing.linkedAt());
    assertThat(link.rationale()).isEqualTo(rationale);
  }

  @Test
  void linkProblemSignal_rejectsBlankRationaleBeforeReading() {
    assertThatThrownBy(() -> service.linkProblemSignal(UUID.randomUUID(), UUID.randomUUID(), " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank rationale is required.");
    verifyNoInteractions(problems, signals, problemSignals);
  }

  @Test
  void linkProblemSignal_rejectsUnknownSignal() {
    var problem = savedProblem();
    var signalId = UUID.randomUUID();
    when(problems.findById(problem.id())).thenReturn(Optional.of(problem));
    when(signals.existsById(signalId)).thenReturn(false);

    assertThatThrownBy(() -> service.linkProblemSignal(problem.id(), signalId, randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Signal not found: " + signalId);
    verifyNoInteractions(problemSignals);
  }

  @Test
  void linkSolutionSignal_rejectsUnknownSolution() {
    var solutionId = UUID.randomUUID();
    when(solutions.findById(solutionId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.linkSolutionSignal(solutionId, UUID.randomUUID(), randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + solutionId);
    verifyNoInteractions(solutionSignals);
  }

  @Test
  void linkSolutionProblem_replacesTheRationaleKeepingIdentityAndLinkTime() {
    var solution = savedSolution();
    var problem = savedProblem();
    var existing =
        new SolutionProblem(
            UUID.randomUUID(), solution.id(), problem.id(), randomString(), Instant.now());
    var rationale = randomString();
    when(solutions.findById(solution.id())).thenReturn(Optional.of(solution));
    when(problems.findById(problem.id())).thenReturn(Optional.of(problem));
    when(solutionProblems.findBySolutionIdAndProblemId(solution.id(), problem.id()))
        .thenReturn(Optional.of(existing));
    when(solutionProblems.save(any(SolutionProblem.class))).thenAnswer(call -> call.getArgument(0));

    var link = service.linkSolutionProblem(solution.id(), problem.id(), rationale);

    assertThat(link.id()).isEqualTo(existing.id());
    assertThat(link.linkedAt()).isEqualTo(existing.linkedAt());
    assertThat(link.rationale()).isEqualTo(rationale);
  }

  @Test
  void unlinkProblemSignal_deletesAndReturnsTheLink() {
    var link =
        new ProblemSignal(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), randomString(), Instant.now());
    when(problemSignals.findById(link.id())).thenReturn(Optional.of(link));

    var removed = service.unlinkProblemSignal(link.id());

    assertThat(removed).isEqualTo(link);
    verify(problemSignals).deleteById(link.id());
  }

  @Test
  void unlinkSolutionSignal_rejectsUnknownLink() {
    var id = UUID.randomUUID();
    when(solutionSignals.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.unlinkSolutionSignal(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("SolutionSignal link not found: " + id);
    verify(solutionSignals, never()).deleteById(any());
  }

  @Test
  void unlinkSolutionProblem_rejectsUnknownLink() {
    var id = UUID.randomUUID();
    when(solutionProblems.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.unlinkSolutionProblem(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("SolutionProblem link not found: " + id);
    verify(solutionProblems, never()).deleteById(any());
  }

  @Test
  void getProblem_collectsLinksDetailsAndLinkedTitles() {
    var problem = savedProblem();
    var signal = new Signal(UUID.randomUUID(), randomString(), randomString(), null, Instant.now());
    var solution = savedSolution();
    var source =
        new ProblemSignal(
            UUID.randomUUID(), problem.id(), signal.id(), randomString(), Instant.now());
    var candidate =
        new SolutionProblem(
            UUID.randomUUID(), solution.id(), problem.id(), randomString(), Instant.now());
    var question =
        new RefinementQuestion(
            UUID.randomUUID(),
            problem.id(),
            null,
            randomString(),
            lab.wide.domain.QuestionStatus.OPEN,
            null,
            null,
            null,
            Instant.now(),
            Instant.now());
    var decision =
        new RefinementDecision(
            UUID.randomUUID(),
            problem.id(),
            null,
            randomString(),
            null,
            null,
            null,
            Instant.now(),
            Instant.now());
    when(problems.findById(problem.id())).thenReturn(Optional.of(problem));
    when(problemSignals.findByProblemIdOrderByLinkedAtAscIdAsc(problem.id()))
        .thenReturn(List.of(source));
    when(solutionProblems.findByProblemIdOrderByLinkedAtAscIdAsc(problem.id()))
        .thenReturn(List.of(candidate));
    when(signals.findAllById(List.of(signal.id()))).thenReturn(List.of(signal));
    when(solutions.findAllById(List.of(solution.id()))).thenReturn(List.of(solution));
    when(details.questionsForProblem(problem.id())).thenReturn(List.of(question));
    when(details.decisionsForProblem(problem.id())).thenReturn(List.of(decision));

    var context = service.getProblem(problem.id());

    assertThat(context.problem()).isEqualTo(problem);
    assertThat(context.signals()).containsExactly(source);
    assertThat(context.solutions()).containsExactly(candidate);
    assertThat(context.questions()).containsExactly(question);
    assertThat(context.decisions()).containsExactly(decision);
    assertThat(context.linkedTitles())
        .containsExactly(
            Map.entry(signal.id(), signal.title()), Map.entry(solution.id(), solution.title()));
  }

  @Test
  void getSolution_rejectsUnknownSolution() {
    var id = UUID.randomUUID();
    when(solutions.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getSolution(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + id);
  }

  @Test
  void problemsForSignal_rejectsUnknownSignal() {
    var id = UUID.randomUUID();
    when(signals.existsById(id)).thenReturn(false);

    assertThatThrownBy(() -> service.problemsForSignal(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Signal not found: " + id);
    verifyNoInteractions(problemSignals);
  }

  @Test
  void solutionsForSignal_returnsLinksInLinkOrder() {
    var signalId = existingSignal();
    var link =
        new SolutionSignal(
            UUID.randomUUID(), UUID.randomUUID(), signalId, randomString(), Instant.now());
    when(solutionSignals.findBySignalIdOrderByLinkedAtAscIdAsc(signalId)).thenReturn(List.of(link));

    assertThat(service.solutionsForSignal(signalId)).containsExactly(link);
  }

  private void savesProblems() {
    when(problems.save(any(Problem.class))).thenAnswer(call -> call.getArgument(0));
  }

  private UUID existingSignal() {
    var id = UUID.randomUUID();
    when(signals.existsById(id)).thenReturn(true);
    return id;
  }

  private Problem savedProblem() {
    var created = Instant.now().minusSeconds(60);
    return new Problem(
        UUID.randomUUID(),
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        created,
        created);
  }

  private Solution savedSolution() {
    var created = Instant.now().minusSeconds(60);
    return new Solution(
        UUID.randomUUID(),
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        created,
        created);
  }
}
