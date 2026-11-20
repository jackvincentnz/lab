package lab.wide.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.time.Instant;
import java.util.UUID;
import lab.test.RequiresDocker;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Signal;
import lab.wide.domain.Solution;
import lab.wide.domain.SolutionProblem;
import lab.wide.domain.SolutionSignal;
import lab.wide.testing.PostgresTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@RequiresDocker
class SearchRepositoryTest extends PostgresTestBase {

  @Autowired SearchRepository searchRepository;

  @Autowired SignalRepository signalRepository;

  @Autowired ProblemRepository problemRepository;

  @Autowired SolutionRepository solutionRepository;

  @Autowired ProblemSignalRepository problemSignalRepository;

  @Autowired SolutionSignalRepository solutionSignalRepository;

  @Autowired SolutionProblemRepository solutionProblemRepository;

  @Autowired RefinementQuestionRepository refinementQuestionRepository;

  @Autowired RefinementDecisionRepository refinementDecisionRepository;

  @Autowired JdbcTemplate jdbcTemplate;

  @BeforeEach
  void clear() {
    clearRecords(jdbcTemplate);
  }

  @Test
  void search_differentCase_matchesLiteralTextAcrossTypes() {
    var needle = randomString();
    var signal = signal(randomString(), randomString() + needle.toUpperCase(), Instant.now());
    var problem = problem(randomString(), Instant.now(), randomString() + needle);
    var solution = solution(randomString(), Instant.now(), needle + randomString());
    problem(randomString(), Instant.now(), randomString());

    var searchRows = searchRepository.search(needle.toLowerCase(), true, true, true, null, 50, 0);

    assertThat(searchRows)
        .extracting(SearchRow::id)
        .containsExactlyInAnyOrder(signal.id(), problem.id(), solution.id());
  }

  @Test
  void search_wildcardCharacters_matchesThemLiterally() {
    var signal = signal(randomString(), "demo 50%_retry latency", Instant.now());
    signal(randomString(), "demo 50 retry latency", Instant.now());

    var searchRows = searchRepository.search("50%_retry", true, false, false, null, 50, 0);

    assertThat(searchRows).extracting(SearchRow::id).containsExactly(signal.id());
  }

  @Test
  void search_firstPage_returnsMostRecentlyUpdatedFirst() {
    signal(randomString(), randomString(), Instant.parse("2026-01-01T00:00:00Z"));
    var problem = problem(randomString(), Instant.parse("2026-01-02T00:00:00Z"), null);
    var solution = solution(randomString(), Instant.parse("2026-01-03T00:00:00Z"), null);

    var searchRows = searchRepository.search("", true, true, true, null, 2, 0);

    assertThat(searchRows).extracting(SearchRow::id).containsExactly(solution.id(), problem.id());
  }

  @Test
  void search_offset_skipsEarlierRows() {
    var signal = signal(randomString(), randomString(), Instant.parse("2026-01-01T00:00:00Z"));
    problem(randomString(), Instant.parse("2026-01-02T00:00:00Z"), null);
    solution(randomString(), Instant.parse("2026-01-03T00:00:00Z"), null);

    var searchRows = searchRepository.search("", true, true, true, null, 2, 2);

    assertThat(searchRows).extracting(SearchRow::id).containsExactly(signal.id());
  }

  @Test
  void search_oneTypeRequested_returnsOnlyThatType() {
    signal(randomString(), randomString(), Instant.now());
    var problem = problem(randomString(), Instant.now(), null);
    solution(randomString(), Instant.now(), null);

    var searchRows = searchRepository.search("", false, true, false, null, 50, 0);

    assertThat(searchRows)
        .extracting(SearchRow::id, SearchRow::type)
        .containsExactly(tuple(problem.id(), "PROBLEM"));
  }

  @Test
  void search_withProblemId_returnsOnlyLinkedCandidates() {
    var problem = problem(randomString(), Instant.now(), null);
    var linkedSolution = solution(randomString(), Instant.now(), null);
    solution(randomString(), Instant.now(), null);
    solutionProblemRepository.save(
        new SolutionProblem(
            null, linkedSolution.id(), problem.id(), randomString(), Instant.now()));

    var searchRows = searchRepository.search("", false, false, true, problem.id(), 50, 0);

    assertThat(searchRows).extracting(SearchRow::id).containsExactly(linkedSolution.id());
  }

  @Test
  void search_problemWithLinksAndDetails_countsThem() {
    var signal = signal(randomString(), randomString(), Instant.now());
    var problem = problem(randomString(), Instant.now(), null);
    var solution = solution(randomString(), Instant.now(), null);
    problemSignalRepository.save(
        new ProblemSignal(null, problem.id(), signal.id(), randomString(), Instant.now()));
    solutionProblemRepository.save(
        new SolutionProblem(null, solution.id(), problem.id(), randomString(), Instant.now()));
    refinementQuestionRepository.save(question(problem.id(), null, QuestionStatus.OPEN));
    refinementQuestionRepository.save(question(problem.id(), null, QuestionStatus.ANSWERED));
    refinementDecisionRepository.save(decision(problem.id(), null));

    var searchRow = searchRepository.search("", false, true, false, null, 50, 0).get(0);

    assertThat(searchRow.signalCount()).isEqualTo(1);
    assertThat(searchRow.problemCount()).isZero();
    assertThat(searchRow.solutionCount()).isEqualTo(1);
    assertThat(searchRow.openQuestionCount()).isEqualTo(1);
    assertThat(searchRow.decisionCount()).isEqualTo(1);
  }

  @Test
  void search_solutionWithLinksAndDetails_countsThem() {
    var signal = signal(randomString(), randomString(), Instant.now());
    var problem = problem(randomString(), Instant.now(), null);
    var solution = solution(randomString(), Instant.now(), null);
    solutionSignalRepository.save(
        new SolutionSignal(null, solution.id(), signal.id(), randomString(), Instant.now()));
    solutionProblemRepository.save(
        new SolutionProblem(null, solution.id(), problem.id(), randomString(), Instant.now()));
    refinementQuestionRepository.save(question(null, solution.id(), QuestionStatus.OPEN));
    refinementDecisionRepository.save(decision(null, solution.id()));

    var searchRow = searchRepository.search("", false, false, true, null, 50, 0).get(0);

    assertThat(searchRow.signalCount()).isEqualTo(1);
    assertThat(searchRow.problemCount()).isEqualTo(1);
    assertThat(searchRow.solutionCount()).isZero();
    assertThat(searchRow.openQuestionCount()).isEqualTo(1);
    assertThat(searchRow.decisionCount()).isEqualTo(1);
  }

  @Test
  void search_linkedSignal_countsProblemsAndSolutions() {
    var signal = signal(randomString(), randomString(), Instant.now());
    var problem = problem(randomString(), Instant.now(), null);
    var solution = solution(randomString(), Instant.now(), null);
    problemSignalRepository.save(
        new ProblemSignal(null, problem.id(), signal.id(), randomString(), Instant.now()));
    solutionSignalRepository.save(
        new SolutionSignal(null, solution.id(), signal.id(), randomString(), Instant.now()));

    var searchRow = searchRepository.search("", true, false, false, null, 50, 0).get(0);

    assertThat(searchRow.signalCount()).isZero();
    assertThat(searchRow.problemCount()).isEqualTo(1);
    assertThat(searchRow.solutionCount()).isEqualTo(1);
    assertThat(searchRow.openQuestionCount()).isZero();
    assertThat(searchRow.decisionCount()).isZero();
  }

  private Signal signal(String title, String content, Instant capturedAt) {
    return signalRepository.save(new Signal(null, title, content, null, capturedAt));
  }

  private Problem problem(String title, Instant updatedAt, String impact) {
    return problemRepository.save(
        new Problem(null, title, randomString(), impact, null, updatedAt, updatedAt));
  }

  private Solution solution(String title, Instant updatedAt, String tradeoffs) {
    return solutionRepository.save(
        new Solution(null, title, randomString(), null, tradeoffs, null, updatedAt, updatedAt));
  }

  private RefinementQuestion question(UUID problemId, UUID solutionId, QuestionStatus status) {
    var answer = status == QuestionStatus.ANSWERED ? randomString() : null;
    return new RefinementQuestion(
        null,
        problemId,
        solutionId,
        randomString(),
        status,
        answer,
        null,
        null,
        Instant.now(),
        Instant.now());
  }

  private RefinementDecision decision(UUID problemId, UUID solutionId) {
    return new RefinementDecision(
        null,
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
