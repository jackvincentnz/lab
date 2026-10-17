package lab.wide.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import lab.wide.domain.Problem;
import lab.wide.domain.ProblemSignal;
import lab.wide.domain.QuestionStatus;
import lab.wide.domain.RefinementDecision;
import lab.wide.domain.RefinementQuestion;
import lab.wide.domain.Signal;
import lab.wide.domain.Solution;
import lab.wide.domain.SolutionProblem;
import lab.wide.testing.ServerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SearchRepositoryTest extends ServerTest {

  @Autowired SearchRepository search;
  @Autowired SignalRepository signals;
  @Autowired ProblemRepository problems;
  @Autowired SolutionRepository solutions;
  @Autowired ProblemSignalRepository problemSignals;
  @Autowired SolutionProblemRepository solutionProblems;
  @Autowired RefinementQuestionRepository questions;
  @Autowired RefinementDecisionRepository decisions;

  @BeforeEach
  void clear() {
    questions.deleteAll();
    decisions.deleteAll();
    solutionProblems.deleteAll();
    problemSignals.deleteAll();
    solutions.deleteAll();
    problems.deleteAll();
    signals.deleteAll();
  }

  @Test
  void search_matchesLiteralTextAcrossTypesIgnoringCase() {
    var needle = randomString();
    var signal = signal(randomString(), "demo " + needle.toUpperCase() + " retry", Instant.now());
    var problem = problem(randomString(), Instant.now(), "Possible " + needle + " impact");
    var solution = solution(randomString(), Instant.now(), "Compare " + needle);
    problem(randomString(), Instant.now(), randomString());

    var rows = search.search(needle.toLowerCase(), true, true, true, null, 50, 0);

    assertThat(rows)
        .extracting(SearchRow::id)
        .containsExactlyInAnyOrder(signal.id(), problem.id(), solution.id());
  }

  @Test
  void search_treatsWildcardCharactersLiterally() {
    var signal = signal(randomString(), "demo 50%_retry latency", Instant.now());
    signal(randomString(), "demo 50 retry latency", Instant.now());

    var rows = search.search("50%_retry", true, false, false, null, 50, 0);

    assertThat(rows).extracting(SearchRow::id).containsExactly(signal.id());
  }

  @Test
  void search_ordersMostRecentlyUpdatedFirstAndPages() {
    var oldest = signal(randomString(), randomString(), Instant.parse("2026-01-01T00:00:00Z"));
    var middle = problem(randomString(), Instant.parse("2026-01-02T00:00:00Z"), null);
    var newest = solution(randomString(), Instant.parse("2026-01-03T00:00:00Z"), null);

    assertThat(search.search("", true, true, true, null, 2, 0))
        .extracting(SearchRow::id)
        .containsExactly(newest.id(), middle.id());
    assertThat(search.search("", true, true, true, null, 2, 2))
        .extracting(SearchRow::id)
        .containsExactly(oldest.id());
  }

  @Test
  void search_filtersByRequestedTypes() {
    signal(randomString(), randomString(), Instant.now());
    var problem = problem(randomString(), Instant.now(), null);
    solution(randomString(), Instant.now(), null);

    var rows = search.search("", false, true, false, null, 50, 0);

    assertThat(rows).extracting(SearchRow::id).containsExactly(problem.id());
    assertThat(rows.get(0).type()).isEqualTo("PROBLEM");
  }

  @Test
  void search_narrowsToCandidatesLinkedToTheProblem() {
    var problem = problem(randomString(), Instant.now(), null);
    var linked = solution(randomString(), Instant.now(), null);
    solution(randomString(), Instant.now(), null);
    solutionProblems.save(
        new SolutionProblem(null, linked.id(), problem.id(), randomString(), Instant.now()));

    var rows = search.search("", false, false, true, problem.id(), 50, 0);

    assertThat(rows).extracting(SearchRow::id).containsExactly(linked.id());
  }

  @Test
  void search_countsLinksOpenQuestionsAndDecisionsPerRecord() {
    var signal = signal(randomString(), randomString(), Instant.now());
    var problem = problem(randomString(), Instant.now(), null);
    var solution = solution(randomString(), Instant.now(), null);
    problemSignals.save(
        new ProblemSignal(null, problem.id(), signal.id(), randomString(), Instant.now()));
    solutionProblems.save(
        new SolutionProblem(null, solution.id(), problem.id(), randomString(), Instant.now()));
    questions.save(question(problem.id(), QuestionStatus.OPEN));
    questions.save(question(problem.id(), QuestionStatus.ANSWERED));
    decisions.save(
        new RefinementDecision(
            null,
            problem.id(),
            null,
            randomString(),
            null,
            null,
            null,
            Instant.now(),
            Instant.now()));

    var row =
        search.search("", false, true, false, null, 50, 0).stream()
            .filter(r -> r.id().equals(problem.id()))
            .findFirst()
            .orElseThrow();

    assertThat(row.signalCount()).isEqualTo(1);
    assertThat(row.solutionCount()).isEqualTo(1);
    assertThat(row.problemCount()).isZero();
    assertThat(row.openQuestionCount()).isEqualTo(1);
    assertThat(row.decisionCount()).isEqualTo(1);
  }

  private Signal signal(String title, String content, Instant capturedAt) {
    return signals.save(new Signal(null, title, content, null, capturedAt));
  }

  private Problem problem(String title, Instant updatedAt, String impact) {
    return problems.save(
        new Problem(null, title, randomString(), impact, null, updatedAt, updatedAt));
  }

  private Solution solution(String title, Instant updatedAt, String tradeoffs) {
    return solutions.save(
        new Solution(null, title, randomString(), null, tradeoffs, null, updatedAt, updatedAt));
  }

  private RefinementQuestion question(UUID problemId, QuestionStatus status) {
    var answer = status == QuestionStatus.ANSWERED ? randomString() : null;
    return new RefinementQuestion(
        null,
        problemId,
        null,
        randomString(),
        status,
        answer,
        null,
        null,
        Instant.now(),
        Instant.now());
  }
}
