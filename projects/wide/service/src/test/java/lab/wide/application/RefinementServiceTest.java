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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lab.test.TestBase;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemRepository;
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.ProblemSignalRepository;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Signal;
import lab.wide.domain.SignalRepository;
import lab.wide.domain.Solution;
import lab.wide.domain.SolutionProblem;
import lab.wide.domain.SolutionProblemRepository;
import lab.wide.domain.SolutionRepository;
import lab.wide.domain.SolutionSignal;
import lab.wide.domain.SolutionSignalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class RefinementServiceTest extends TestBase {

  @Mock ProblemRepository problemRepository;

  @Mock SolutionRepository solutionRepository;

  @Mock SignalRepository signalRepository;

  @Mock ProblemSignalRepository problemSignalRepository;

  @Mock SolutionSignalRepository solutionSignalRepository;

  @Mock SolutionProblemRepository solutionProblemRepository;

  @Mock RefinementDetailService refinementDetailService;

  final Clock clock = fixedClock();

  RefinementService refinementService;

  @Captor ArgumentCaptor<ProblemSignal> problemSignalCaptor;

  @Captor ArgumentCaptor<SolutionSignal> solutionSignalCaptor;

  @Captor ArgumentCaptor<SolutionProblem> solutionProblemCaptor;

  @BeforeEach
  void createService() {
    refinementService =
        new RefinementService(
            problemRepository,
            solutionRepository,
            signalRepository,
            problemSignalRepository,
            solutionSignalRepository,
            solutionProblemRepository,
            refinementDetailService,
            clock);
  }

  @Test
  void getProblem_linkedRecords_returnsLinksDetailsAndLinkedTitles() {
    var problem = existingProblem();
    var signal = signal();
    var solution = solution();
    var problemSignal = problemSignal(problem.id(), signal.id());
    var solutionProblem = solutionProblem(solution.id(), problem.id());
    var refinementQuestion = question(problem.id(), null);
    var refinementDecision = decision(problem.id(), null);
    when(problemSignalRepository.findByProblemIdOrderByLinkedAtAscIdAsc(problem.id()))
        .thenReturn(List.of(problemSignal));
    when(solutionProblemRepository.findByProblemIdOrderByLinkedAtAscIdAsc(problem.id()))
        .thenReturn(List.of(solutionProblem));
    when(signalRepository.findAllById(List.of(signal.id()))).thenReturn(List.of(signal));
    when(solutionRepository.findAllById(List.of(solution.id()))).thenReturn(List.of(solution));
    when(refinementDetailService.questionsForProblem(problem.id()))
        .thenReturn(List.of(refinementQuestion));
    when(refinementDetailService.decisionsForProblem(problem.id()))
        .thenReturn(List.of(refinementDecision));

    var problemContext = refinementService.getProblem(problem.id());

    assertThat(problemContext)
        .isEqualTo(
            new ProblemContext(
                problem,
                List.of(problemSignal),
                List.of(solutionProblem),
                List.of(refinementQuestion),
                List.of(refinementDecision),
                Map.of(signal.id(), signal.title(), solution.id(), solution.title())));
  }

  @Test
  void getProblem_unknownId_throwsNotFound() {
    var id = unknownProblemId();

    assertThatThrownBy(() -> refinementService.getProblem(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Problem not found: " + id);
  }

  @Test
  void getSolution_linkedRecords_returnsLinksDetailsAndLinkedTitles() {
    var solution = existingSolution();
    var signal = signal();
    var problem = problem();
    var solutionSignal = solutionSignal(solution.id(), signal.id());
    var solutionProblem = solutionProblem(solution.id(), problem.id());
    var refinementQuestion = question(null, solution.id());
    var refinementDecision = decision(null, solution.id());
    when(solutionSignalRepository.findBySolutionIdOrderByLinkedAtAscIdAsc(solution.id()))
        .thenReturn(List.of(solutionSignal));
    when(solutionProblemRepository.findBySolutionIdOrderByLinkedAtAscIdAsc(solution.id()))
        .thenReturn(List.of(solutionProblem));
    when(signalRepository.findAllById(List.of(signal.id()))).thenReturn(List.of(signal));
    when(problemRepository.findAllById(List.of(problem.id()))).thenReturn(List.of(problem));
    when(refinementDetailService.questionsForSolution(solution.id()))
        .thenReturn(List.of(refinementQuestion));
    when(refinementDetailService.decisionsForSolution(solution.id()))
        .thenReturn(List.of(refinementDecision));

    var solutionContext = refinementService.getSolution(solution.id());

    assertThat(solutionContext)
        .isEqualTo(
            new SolutionContext(
                solution,
                List.of(solutionSignal),
                List.of(solutionProblem),
                List.of(refinementQuestion),
                List.of(refinementDecision),
                Map.of(signal.id(), signal.title(), problem.id(), problem.title())));
  }

  @Test
  void getSolution_unknownId_throwsNotFound() {
    var id = unknownSolutionId();

    assertThatThrownBy(() -> refinementService.getSolution(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + id);
  }

  @Test
  void problemsForSignal_existingSignal_returnsItsLinks() {
    var signalId = existingSignalId();
    var problemSignal = problemSignal(UUID.randomUUID(), signalId);
    when(problemSignalRepository.findBySignalIdOrderByLinkedAtAscIdAsc(signalId))
        .thenReturn(List.of(problemSignal));

    var problemSignals = refinementService.problemsForSignal(signalId);

    assertThat(problemSignals).containsExactly(problemSignal);
  }

  @Test
  void problemsForSignal_unknownSignal_throwsNotFound() {
    var signalId = UUID.randomUUID();

    assertThatThrownBy(() -> refinementService.problemsForSignal(signalId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Signal not found: " + signalId);
    verifyNoInteractions(problemSignalRepository);
  }

  @Test
  void solutionsForSignal_existingSignal_returnsItsLinks() {
    var signalId = existingSignalId();
    var solutionSignal = solutionSignal(UUID.randomUUID(), signalId);
    when(solutionSignalRepository.findBySignalIdOrderByLinkedAtAscIdAsc(signalId))
        .thenReturn(List.of(solutionSignal));

    var solutionSignals = refinementService.solutionsForSignal(signalId);

    assertThat(solutionSignals).containsExactly(solutionSignal);
  }

  @Test
  void solutionsForSignal_unknownSignal_throwsNotFound() {
    var signalId = UUID.randomUUID();

    assertThatThrownBy(() -> refinementService.solutionsForSignal(signalId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Signal not found: " + signalId);
    verifyNoInteractions(solutionSignalRepository);
  }

  @Test
  void listProblems_limitAndPage_requestsThatPageMostRecentlyUpdatedFirst() {
    var problem = problem();
    when(problemRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(problem)));

    var problems = refinementService.listProblems(10, 1);

    assertThat(problems).containsExactly(problem);
    verify(problemRepository)
        .findAll(PageRequest.of(1, 10, Sort.by("updatedAt", "id").descending()));
  }

  @Test
  void listProblems_noLimitOrPage_requestsTheFirstFifty() {
    when(problemRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

    refinementService.listProblems(null, null);

    verify(problemRepository)
        .findAll(PageRequest.of(0, 50, Sort.by("updatedAt", "id").descending()));
  }

  @Test
  void listProblems_limitBelowOne_throwsWithoutReading() {
    assertThatThrownBy(() -> refinementService.listProblems(0, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–100 and page must be nonnegative.");
    verifyNoInteractions(problemRepository);
  }

  @Test
  void listProblems_limitOverOneHundred_throwsWithoutReading() {
    assertThatThrownBy(() -> refinementService.listProblems(101, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–100 and page must be nonnegative.");
    verifyNoInteractions(problemRepository);
  }

  @Test
  void listProblems_negativePage_throwsWithoutReading() {
    assertThatThrownBy(() -> refinementService.listProblems(1, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Limit must be 1–100 and page must be nonnegative.");
    verifyNoInteractions(problemRepository);
  }

  @Test
  void listSolutions_limitAndPage_requestsThatPageMostRecentlyUpdatedFirst() {
    var solution = solution();
    when(solutionRepository.findAll(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(solution)));

    var solutions = refinementService.listSolutions(5, 2);

    assertThat(solutions).containsExactly(solution);
    verify(solutionRepository)
        .findAll(PageRequest.of(2, 5, Sort.by("updatedAt", "id").descending()));
  }

  @Test
  void listSolutions_negativePage_throwsWithoutReading() {
    assertThatThrownBy(() -> refinementService.listSolutions(1, -1))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(solutionRepository);
  }

  @Test
  void createProblem_validFields_savesANewRecordWithMatchingTimestamps() {
    var title = randomString();
    var description = randomString();
    var impact = randomString();
    var desiredOutcome = randomString();
    savesProblems();

    var problem = refinementService.createProblem(title, description, impact, desiredOutcome);

    assertThat(problem.id()).isNull();
    assertThat(problem.title()).isEqualTo(title);
    assertThat(problem.description()).isEqualTo(description);
    assertThat(problem.impact()).isEqualTo(impact);
    assertThat(problem.desiredOutcome()).isEqualTo(desiredOutcome);
    assertThat(problem.createdAt()).isEqualTo(clock.instant());
    assertThat(problem.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void createProblem_nullTitle_throwsWithoutSaving() {
    assertThatThrownBy(() -> refinementService.createProblem(null, randomString(), null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verifyNoInteractions(problemRepository);
  }

  @Test
  void createProblem_blankDescription_throwsWithoutSaving() {
    assertThatThrownBy(() -> refinementService.createProblem(randomString(), " ", null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank description is required.");
    verifyNoInteractions(problemRepository);
  }

  @Test
  void createProblem_signalLink_linksTheSignalToTheDraft() {
    var problem = existingProblem();
    var signalId = existingSignalId();
    var rationale = randomString();
    when(problemRepository.save(any(Problem.class))).thenReturn(problem);

    refinementService.createProblem(
        randomString(), randomString(), null, null, List.of(new SignalLink(signalId, rationale)));

    verify(problemSignalRepository).save(problemSignalCaptor.capture());
    assertThat(problemSignalCaptor.getValue().problemId()).isEqualTo(problem.id());
    assertThat(problemSignalCaptor.getValue().signalId()).isEqualTo(signalId);
    assertThat(problemSignalCaptor.getValue().rationale()).isEqualTo(rationale);
  }

  @Test
  void createProblem_noSignalLinks_savesOnlyTheDraft() {
    var problem = problem();
    when(problemRepository.save(any(Problem.class))).thenReturn(problem);

    var created = refinementService.createProblem(randomString(), randomString(), null, null, null);

    assertThat(created).isEqualTo(problem);
    verifyNoInteractions(problemSignalRepository);
  }

  @Test
  void createProblem_signalLinkWithoutSignalId_throwsWithoutLinking() {
    var signalLinks = List.of(new SignalLink(null, randomString()));

    assertThatThrownBy(
            () ->
                refinementService.createProblem(
                    randomString(), randomString(), null, null, signalLinks))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Each signal link requires a signalId and rationale.");
    verifyNoInteractions(problemSignalRepository);
  }

  @Test
  void createProblem_nullSignalLink_throwsWithoutLinking() {
    var signalLinks = Arrays.<SignalLink>asList((SignalLink) null);

    assertThatThrownBy(
            () ->
                refinementService.createProblem(
                    randomString(), randomString(), null, null, signalLinks))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Each signal link requires a signalId and rationale.");
    verifyNoInteractions(problemSignalRepository);
  }

  @Test
  void createSolution_validFields_savesANewRecordWithMatchingTimestamps() {
    var title = randomString();
    var approach = randomString();
    var scope = randomString();
    var tradeoffs = randomString();
    var effort = randomString();
    savesSolutions();

    var solution = refinementService.createSolution(title, approach, scope, tradeoffs, effort);

    assertThat(solution.id()).isNull();
    assertThat(solution.title()).isEqualTo(title);
    assertThat(solution.approach()).isEqualTo(approach);
    assertThat(solution.scope()).isEqualTo(scope);
    assertThat(solution.tradeoffs()).isEqualTo(tradeoffs);
    assertThat(solution.effort()).isEqualTo(effort);
    assertThat(solution.createdAt()).isEqualTo(clock.instant());
    assertThat(solution.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void createSolution_nullTitle_throwsWithoutSaving() {
    assertThatThrownBy(
            () -> refinementService.createSolution(null, randomString(), null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verifyNoInteractions(solutionRepository);
  }

  @Test
  void createSolution_blankApproach_throwsWithoutSaving() {
    assertThatThrownBy(
            () -> refinementService.createSolution(randomString(), " ", null, null, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank approach is required.");
    verifyNoInteractions(solutionRepository);
  }

  @Test
  void createSolution_signalAndProblemLinks_linksEachToTheDraft() {
    var solution = existingSolution();
    var problem = existingProblem();
    var signalId = existingSignalId();
    when(solutionRepository.save(any(Solution.class))).thenReturn(solution);

    refinementService.createSolution(
        randomString(),
        randomString(),
        null,
        null,
        null,
        List.of(new SignalLink(signalId, randomString())),
        List.of(new ProblemLink(problem.id(), randomString())));

    verify(solutionSignalRepository).save(solutionSignalCaptor.capture());
    verify(solutionProblemRepository).save(solutionProblemCaptor.capture());
    assertThat(solutionSignalCaptor.getValue().solutionId()).isEqualTo(solution.id());
    assertThat(solutionSignalCaptor.getValue().signalId()).isEqualTo(signalId);
    assertThat(solutionProblemCaptor.getValue().solutionId()).isEqualTo(solution.id());
    assertThat(solutionProblemCaptor.getValue().problemId()).isEqualTo(problem.id());
  }

  @Test
  void createSolution_noLinks_savesOnlyTheDraft() {
    var solution = solution();
    when(solutionRepository.save(any(Solution.class))).thenReturn(solution);

    var created =
        refinementService.createSolution(
            randomString(), randomString(), null, null, null, null, null);

    assertThat(created).isEqualTo(solution);
    verifyNoInteractions(solutionSignalRepository, solutionProblemRepository);
  }

  @Test
  void createSolution_signalLinkWithoutSignalId_throwsWithoutLinking() {
    var signalLinks = List.of(new SignalLink(null, randomString()));

    assertThatThrownBy(
            () ->
                refinementService.createSolution(
                    randomString(), randomString(), null, null, null, signalLinks, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Each signal link requires a signalId and rationale.");
    verifyNoInteractions(solutionSignalRepository);
  }

  @Test
  void createSolution_nullSignalLink_throwsWithoutLinking() {
    var signalLinks = Arrays.<SignalLink>asList((SignalLink) null);

    assertThatThrownBy(
            () ->
                refinementService.createSolution(
                    randomString(), randomString(), null, null, null, signalLinks, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Each signal link requires a signalId and rationale.");
    verifyNoInteractions(solutionSignalRepository);
  }

  @Test
  void createSolution_problemLinkWithoutProblemId_throwsWithoutLinking() {
    var problemLinks = List.of(new ProblemLink(null, randomString()));

    assertThatThrownBy(
            () ->
                refinementService.createSolution(
                    randomString(), randomString(), null, null, null, null, problemLinks))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Each problem link requires a problemId and rationale.");
    verifyNoInteractions(solutionProblemRepository);
  }

  @Test
  void createSolution_nullProblemLink_throwsWithoutLinking() {
    var problemLinks = Arrays.<ProblemLink>asList((ProblemLink) null);

    assertThatThrownBy(
            () ->
                refinementService.createSolution(
                    randomString(), randomString(), null, null, null, null, problemLinks))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Each problem link requires a problemId and rationale.");
    verifyNoInteractions(solutionProblemRepository);
  }

  @Test
  void editProblem_newTitle_keepsOmittedFieldsIdentityAndCreationTime() {
    var problem = existingProblem();
    var title = randomString();
    savesProblems();

    var edited = refinementService.editProblem(problem.id(), Map.of("title", title), null);

    assertThat(edited.id()).isEqualTo(problem.id());
    assertThat(edited.title()).isEqualTo(title);
    assertThat(edited.description()).isEqualTo(problem.description());
    assertThat(edited.impact()).isEqualTo(problem.impact());
    assertThat(edited.desiredOutcome()).isEqualTo(problem.desiredOutcome());
    assertThat(edited.createdAt()).isEqualTo(problem.createdAt());
    assertThat(edited.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void editProblem_validEdit_readsTheProblemOnce() {
    var problem = existingProblem();
    savesProblems();

    refinementService.editProblem(problem.id(), Map.of("description", randomString()), null);

    verify(problemRepository).findById(problem.id());
  }

  @Test
  void editProblem_clearedOptionalFields_savesThemEmpty() {
    var problem = existingProblem();
    savesProblems();

    var edited =
        refinementService.editProblem(problem.id(), null, List.of("impact", "desiredOutcome"));

    assertThat(edited.impact()).isNull();
    assertThat(edited.desiredOutcome()).isNull();
  }

  @Test
  void editProblem_blankTitle_throwsWithoutSaving() {
    var problem = existingProblem();

    assertThatThrownBy(
            () -> refinementService.editProblem(problem.id(), Map.of("title", " "), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verify(problemRepository, never()).save(any());
  }

  @Test
  void editProblem_blankDescription_throwsWithoutSaving() {
    var problem = existingProblem();

    assertThatThrownBy(
            () -> refinementService.editProblem(problem.id(), Map.of("description", " "), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank description is required.");
    verify(problemRepository, never()).save(any());
  }

  @Test
  void editProblem_unknownField_throwsBeforeReading() {
    var fields = Map.of("typo", randomString());

    assertThatThrownBy(() -> refinementService.editProblem(UUID.randomUUID(), fields, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageStartingWith("Unknown editable field: typo");
    verifyNoInteractions(problemRepository);
  }

  @Test
  void editProblem_unknownId_throwsNotFound() {
    var id = unknownProblemId();

    assertThatThrownBy(
            () -> refinementService.editProblem(id, Map.of("title", randomString()), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Problem not found: " + id);
  }

  @Test
  void editSolution_newApproachAndClearedFields_keepsTheRest() {
    var solution = existingSolution();
    var approach = randomString();
    savesSolutions();

    var edited =
        refinementService.editSolution(
            solution.id(), Map.of("approach", approach), List.of("scope", "tradeoffs", "effort"));

    assertThat(edited.id()).isEqualTo(solution.id());
    assertThat(edited.title()).isEqualTo(solution.title());
    assertThat(edited.approach()).isEqualTo(approach);
    assertThat(edited.scope()).isNull();
    assertThat(edited.tradeoffs()).isNull();
    assertThat(edited.effort()).isNull();
    assertThat(edited.createdAt()).isEqualTo(solution.createdAt());
    assertThat(edited.updatedAt()).isEqualTo(clock.instant());
  }

  @Test
  void editSolution_validEdit_readsTheSolutionOnce() {
    var solution = existingSolution();
    savesSolutions();

    refinementService.editSolution(solution.id(), Map.of("title", randomString()), null);

    verify(solutionRepository).findById(solution.id());
  }

  @Test
  void editSolution_blankTitle_throwsWithoutSaving() {
    var solution = existingSolution();

    assertThatThrownBy(
            () -> refinementService.editSolution(solution.id(), Map.of("title", " "), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank title is required.");
    verify(solutionRepository, never()).save(any());
  }

  @Test
  void editSolution_blankApproach_throwsWithoutSaving() {
    var solution = existingSolution();

    assertThatThrownBy(
            () -> refinementService.editSolution(solution.id(), Map.of("approach", " "), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank approach is required.");
    verify(solutionRepository, never()).save(any());
  }

  @Test
  void editSolution_unknownId_throwsNotFound() {
    var id = unknownSolutionId();

    assertThatThrownBy(
            () -> refinementService.editSolution(id, Map.of("title", randomString()), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + id);
  }

  @Test
  void linkProblemSignal_newPair_createsALinkWithItsOwnLinkTime() {
    var problem = existingProblem();
    var signalId = existingSignalId();
    var rationale = randomString();
    when(problemSignalRepository.save(any(ProblemSignal.class))).thenAnswer(returnsFirstArg());

    var problemSignal = refinementService.linkProblemSignal(problem.id(), signalId, rationale);

    assertThat(problemSignal.id()).isNull();
    assertThat(problemSignal.problemId()).isEqualTo(problem.id());
    assertThat(problemSignal.signalId()).isEqualTo(signalId);
    assertThat(problemSignal.rationale()).isEqualTo(rationale);
    assertThat(problemSignal.linkedAt()).isEqualTo(clock.instant());
  }

  @Test
  void linkProblemSignal_linkedPair_replacesTheRationaleKeepingIdentityAndLinkTime() {
    var problem = existingProblem();
    var signalId = existingSignalId();
    var existing = problemSignal(problem.id(), signalId);
    var rationale = randomString();
    when(problemSignalRepository.findByProblemIdAndSignalId(problem.id(), signalId))
        .thenReturn(Optional.of(existing));
    when(problemSignalRepository.save(any(ProblemSignal.class))).thenAnswer(returnsFirstArg());

    var problemSignal = refinementService.linkProblemSignal(problem.id(), signalId, rationale);

    assertThat(problemSignal)
        .isEqualTo(
            new ProblemSignal(
                existing.id(), problem.id(), signalId, rationale, existing.linkedAt()));
  }

  @Test
  void linkProblemSignal_blankRationale_throwsBeforeReading() {
    assertThatThrownBy(
            () -> refinementService.linkProblemSignal(UUID.randomUUID(), UUID.randomUUID(), " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Nonblank rationale is required.");
    verifyNoInteractions(problemRepository, signalRepository, problemSignalRepository);
  }

  @Test
  void linkProblemSignal_unknownProblem_throwsWithoutLinking() {
    var problemId = unknownProblemId();

    assertThatThrownBy(
            () -> refinementService.linkProblemSignal(problemId, UUID.randomUUID(), randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Problem not found: " + problemId);
    verifyNoInteractions(problemSignalRepository);
  }

  @Test
  void linkProblemSignal_unknownSignal_throwsWithoutLinking() {
    var problem = existingProblem();
    var signalId = UUID.randomUUID();

    assertThatThrownBy(
            () -> refinementService.linkProblemSignal(problem.id(), signalId, randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Signal not found: " + signalId);
    verifyNoInteractions(problemSignalRepository);
  }

  @Test
  void problemSignal_existingId_returnsTheLink() {
    var problemSignal = problemSignal(UUID.randomUUID(), UUID.randomUUID());
    when(problemSignalRepository.findById(problemSignal.id()))
        .thenReturn(Optional.of(problemSignal));

    var found = refinementService.problemSignal(problemSignal.id());

    assertThat(found).isEqualTo(problemSignal);
  }

  @Test
  void problemSignal_unknownId_throwsNotFound() {
    var id = UUID.randomUUID();

    assertThatThrownBy(() -> refinementService.problemSignal(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("ProblemSignal link not found: " + id);
  }

  @Test
  void unlinkProblemSignal_existingId_deletesAndReturnsTheLink() {
    var problemSignal = problemSignal(UUID.randomUUID(), UUID.randomUUID());
    when(problemSignalRepository.findById(problemSignal.id()))
        .thenReturn(Optional.of(problemSignal));

    var removed = refinementService.unlinkProblemSignal(problemSignal.id());

    assertThat(removed).isEqualTo(problemSignal);
    verify(problemSignalRepository).deleteById(problemSignal.id());
  }

  @Test
  void unlinkProblemSignal_unknownId_throwsWithoutDeleting() {
    var id = UUID.randomUUID();

    assertThatThrownBy(() -> refinementService.unlinkProblemSignal(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("ProblemSignal link not found: " + id);
    verify(problemSignalRepository, never()).deleteById(any());
  }

  @Test
  void linkSolutionSignal_newPair_createsALinkWithItsOwnLinkTime() {
    var solution = existingSolution();
    var signalId = existingSignalId();
    var rationale = randomString();
    when(solutionSignalRepository.save(any(SolutionSignal.class))).thenAnswer(returnsFirstArg());

    var solutionSignal = refinementService.linkSolutionSignal(solution.id(), signalId, rationale);

    assertThat(solutionSignal.id()).isNull();
    assertThat(solutionSignal.solutionId()).isEqualTo(solution.id());
    assertThat(solutionSignal.signalId()).isEqualTo(signalId);
    assertThat(solutionSignal.rationale()).isEqualTo(rationale);
    assertThat(solutionSignal.linkedAt()).isEqualTo(clock.instant());
  }

  @Test
  void linkSolutionSignal_linkedPair_replacesTheRationaleKeepingIdentityAndLinkTime() {
    var solution = existingSolution();
    var signalId = existingSignalId();
    var existing = solutionSignal(solution.id(), signalId);
    var rationale = randomString();
    when(solutionSignalRepository.findBySolutionIdAndSignalId(solution.id(), signalId))
        .thenReturn(Optional.of(existing));
    when(solutionSignalRepository.save(any(SolutionSignal.class))).thenAnswer(returnsFirstArg());

    var solutionSignal = refinementService.linkSolutionSignal(solution.id(), signalId, rationale);

    assertThat(solutionSignal)
        .isEqualTo(
            new SolutionSignal(
                existing.id(), solution.id(), signalId, rationale, existing.linkedAt()));
  }

  @Test
  void linkSolutionSignal_unknownSolution_throwsWithoutLinking() {
    var solutionId = unknownSolutionId();

    assertThatThrownBy(
            () ->
                refinementService.linkSolutionSignal(solutionId, UUID.randomUUID(), randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + solutionId);
    verifyNoInteractions(solutionSignalRepository);
  }

  @Test
  void linkSolutionSignal_unknownSignal_throwsWithoutLinking() {
    var solution = existingSolution();
    var signalId = UUID.randomUUID();

    assertThatThrownBy(
            () -> refinementService.linkSolutionSignal(solution.id(), signalId, randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Signal not found: " + signalId);
    verifyNoInteractions(solutionSignalRepository);
  }

  @Test
  void solutionSignal_existingId_returnsTheLink() {
    var solutionSignal = solutionSignal(UUID.randomUUID(), UUID.randomUUID());
    when(solutionSignalRepository.findById(solutionSignal.id()))
        .thenReturn(Optional.of(solutionSignal));

    var found = refinementService.solutionSignal(solutionSignal.id());

    assertThat(found).isEqualTo(solutionSignal);
  }

  @Test
  void unlinkSolutionSignal_existingId_deletesAndReturnsTheLink() {
    var solutionSignal = solutionSignal(UUID.randomUUID(), UUID.randomUUID());
    when(solutionSignalRepository.findById(solutionSignal.id()))
        .thenReturn(Optional.of(solutionSignal));

    var removed = refinementService.unlinkSolutionSignal(solutionSignal.id());

    assertThat(removed).isEqualTo(solutionSignal);
    verify(solutionSignalRepository).deleteById(solutionSignal.id());
  }

  @Test
  void unlinkSolutionSignal_unknownId_throwsWithoutDeleting() {
    var id = UUID.randomUUID();

    assertThatThrownBy(() -> refinementService.unlinkSolutionSignal(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("SolutionSignal link not found: " + id);
    verify(solutionSignalRepository, never()).deleteById(any());
  }

  @Test
  void linkSolutionProblem_newPair_createsALinkWithItsOwnLinkTime() {
    var solution = existingSolution();
    var problem = existingProblem();
    var rationale = randomString();
    when(solutionProblemRepository.save(any(SolutionProblem.class))).thenAnswer(returnsFirstArg());

    var solutionProblem =
        refinementService.linkSolutionProblem(solution.id(), problem.id(), rationale);

    assertThat(solutionProblem.id()).isNull();
    assertThat(solutionProblem.solutionId()).isEqualTo(solution.id());
    assertThat(solutionProblem.problemId()).isEqualTo(problem.id());
    assertThat(solutionProblem.rationale()).isEqualTo(rationale);
    assertThat(solutionProblem.linkedAt()).isEqualTo(clock.instant());
  }

  @Test
  void linkSolutionProblem_linkedPair_replacesTheRationaleKeepingIdentityAndLinkTime() {
    var solution = existingSolution();
    var problem = existingProblem();
    var existing = solutionProblem(solution.id(), problem.id());
    var rationale = randomString();
    when(solutionProblemRepository.findBySolutionIdAndProblemId(solution.id(), problem.id()))
        .thenReturn(Optional.of(existing));
    when(solutionProblemRepository.save(any(SolutionProblem.class))).thenAnswer(returnsFirstArg());

    var solutionProblem =
        refinementService.linkSolutionProblem(solution.id(), problem.id(), rationale);

    assertThat(solutionProblem)
        .isEqualTo(
            new SolutionProblem(
                existing.id(), solution.id(), problem.id(), rationale, existing.linkedAt()));
  }

  @Test
  void linkSolutionProblem_unknownSolution_throwsWithoutLinking() {
    var solutionId = unknownSolutionId();

    assertThatThrownBy(
            () ->
                refinementService.linkSolutionProblem(
                    solutionId, UUID.randomUUID(), randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Solution not found: " + solutionId);
    verifyNoInteractions(solutionProblemRepository);
  }

  @Test
  void linkSolutionProblem_unknownProblem_throwsWithoutLinking() {
    var solution = existingSolution();
    var problemId = unknownProblemId();

    assertThatThrownBy(
            () -> refinementService.linkSolutionProblem(solution.id(), problemId, randomString()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Problem not found: " + problemId);
    verifyNoInteractions(solutionProblemRepository);
  }

  @Test
  void solutionProblem_existingId_returnsTheLink() {
    var solutionProblem = solutionProblem(UUID.randomUUID(), UUID.randomUUID());
    when(solutionProblemRepository.findById(solutionProblem.id()))
        .thenReturn(Optional.of(solutionProblem));

    var found = refinementService.solutionProblem(solutionProblem.id());

    assertThat(found).isEqualTo(solutionProblem);
  }

  @Test
  void unlinkSolutionProblem_existingId_deletesAndReturnsTheLink() {
    var solutionProblem = solutionProblem(UUID.randomUUID(), UUID.randomUUID());
    when(solutionProblemRepository.findById(solutionProblem.id()))
        .thenReturn(Optional.of(solutionProblem));

    var removed = refinementService.unlinkSolutionProblem(solutionProblem.id());

    assertThat(removed).isEqualTo(solutionProblem);
    verify(solutionProblemRepository).deleteById(solutionProblem.id());
  }

  @Test
  void unlinkSolutionProblem_unknownId_throwsWithoutDeleting() {
    var id = UUID.randomUUID();

    assertThatThrownBy(() -> refinementService.unlinkSolutionProblem(id))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("SolutionProblem link not found: " + id);
    verify(solutionProblemRepository, never()).deleteById(any());
  }

  private void savesProblems() {
    when(problemRepository.save(any(Problem.class))).thenAnswer(returnsFirstArg());
  }

  private void savesSolutions() {
    when(solutionRepository.save(any(Solution.class))).thenAnswer(returnsFirstArg());
  }

  private Problem existingProblem() {
    var problem = problem();
    when(problemRepository.findById(problem.id())).thenReturn(Optional.of(problem));
    return problem;
  }

  private UUID unknownProblemId() {
    var id = UUID.randomUUID();
    when(problemRepository.findById(id)).thenReturn(Optional.empty());
    return id;
  }

  private Solution existingSolution() {
    var solution = solution();
    when(solutionRepository.findById(solution.id())).thenReturn(Optional.of(solution));
    return solution;
  }

  private UUID unknownSolutionId() {
    var id = UUID.randomUUID();
    when(solutionRepository.findById(id)).thenReturn(Optional.empty());
    return id;
  }

  private UUID existingSignalId() {
    var id = UUID.randomUUID();
    when(signalRepository.existsById(id)).thenReturn(true);
    return id;
  }

  private Problem problem() {
    var createdAt = clock.instant().minusSeconds(60);
    return new Problem(
        UUID.randomUUID(),
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        createdAt,
        createdAt);
  }

  private Solution solution() {
    var createdAt = clock.instant().minusSeconds(60);
    return new Solution(
        UUID.randomUUID(),
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        randomString(),
        createdAt,
        createdAt);
  }

  private Signal signal() {
    return new Signal(UUID.randomUUID(), randomString(), randomString(), null, clock.instant());
  }

  private ProblemSignal problemSignal(UUID problemId, UUID signalId) {
    return new ProblemSignal(
        UUID.randomUUID(), problemId, signalId, randomString(), clock.instant().minusSeconds(60));
  }

  private SolutionSignal solutionSignal(UUID solutionId, UUID signalId) {
    return new SolutionSignal(
        UUID.randomUUID(), solutionId, signalId, randomString(), clock.instant().minusSeconds(60));
  }

  private SolutionProblem solutionProblem(UUID solutionId, UUID problemId) {
    return new SolutionProblem(
        UUID.randomUUID(), solutionId, problemId, randomString(), clock.instant().minusSeconds(60));
  }

  private RefinementQuestion question(UUID problemId, UUID solutionId) {
    return new RefinementQuestion(
        UUID.randomUUID(),
        problemId,
        solutionId,
        randomString(),
        QuestionStatus.OPEN,
        null,
        null,
        null,
        clock.instant(),
        clock.instant());
  }

  private RefinementDecision decision(UUID problemId, UUID solutionId) {
    return new RefinementDecision(
        UUID.randomUUID(),
        problemId,
        solutionId,
        randomString(),
        null,
        null,
        null,
        clock.instant(),
        clock.instant());
  }
}
