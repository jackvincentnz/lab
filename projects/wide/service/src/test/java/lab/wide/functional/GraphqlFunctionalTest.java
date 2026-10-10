package lab.wide.functional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.wide.application.RefinementDetailService;
import lab.wide.application.RefinementService;
import lab.wide.application.SignalService;
import lab.wide.domain.QuestionStatus;
import lab.wide.infrastructure.ProblemRepository;
import lab.wide.infrastructure.ProblemSignalRepository;
import lab.wide.infrastructure.RefinementDecisionRepository;
import lab.wide.infrastructure.RefinementQuestionRepository;
import lab.wide.infrastructure.SignalRepository;
import lab.wide.infrastructure.SolutionProblemRepository;
import lab.wide.infrastructure.SolutionRepository;
import lab.wide.infrastructure.SolutionSignalRepository;
import lab.wide.testing.ServerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

class GraphqlFunctionalTest extends ServerTest {

  @Autowired SignalService intake;
  @Autowired RefinementService refinement;
  @Autowired RefinementDetailService details;
  @Autowired SignalRepository signals;
  @Autowired ProblemRepository problems;
  @Autowired SolutionRepository solutions;
  @Autowired ProblemSignalRepository problemSignals;
  @Autowired SolutionSignalRepository solutionSignals;
  @Autowired SolutionProblemRepository solutionProblems;
  @Autowired RefinementQuestionRepository questions;
  @Autowired RefinementDecisionRepository decisions;

  @BeforeEach
  void clear() {
    questions.deleteAll();
    decisions.deleteAll();
    solutionProblems.deleteAll();
    solutionSignals.deleteAll();
    problemSignals.deleteAll();
    solutions.deleteAll();
    problems.deleteAll();
    signals.deleteAll();
  }

  @Test
  void query_readsLinkedContextInBothDirections() {
    var signal = intake.capture(randomString(), randomString(), randomString());
    var problem = refinement.createProblem(randomString(), randomString(), null, null);
    var solution = refinement.createSolution(randomString(), randomString(), null, null, null);
    var source = refinement.linkProblemSignal(problem.id(), signal.id(), randomString());
    refinement.linkSolutionSignal(solution.id(), signal.id(), randomString());
    refinement.linkSolutionProblem(solution.id(), problem.id(), randomString());
    var question =
        details.createQuestion(
            problem.id(),
            null,
            randomString(),
            QuestionStatus.ANSWERED,
            randomString(),
            null,
            null);
    var decision =
        details.createDecision(null, solution.id(), randomString(), null, randomString(), null);

    var result =
        query(
            """
            query Context($signal: ID!, $problem: ID!, $solution: ID!) {
              signal(id: $signal) {
                content capturedAt
                problems { id rationale problem { id } }
                solutions { solution { id } }
              }
              problem(id: $problem) {
                questions { id status answer }
                signals { signal { id content } }
                solutions { solution { id approach } }
              }
              solution(id: $solution) {
                decisions { id source }
                problems { problem { id } }
              }
            }
            """,
            Map.of("signal", signal.id(), "problem", problem.id(), "solution", solution.id()));

    assertThat(result.get("errors")).isNull();
    var data = map(result.get("data"));
    var fetchedSignal = map(data.get("signal"));
    assertThat(fetchedSignal.get("content")).isEqualTo(signal.content());
    // Postgres stores microseconds, so compare with the persisted value rather than the JVM clock.
    assertThat(fetchedSignal.get("capturedAt"))
        .isEqualTo(intake.get(signal.id()).capturedAt().toString());
    var sourceLink = map(list(fetchedSignal.get("problems")).get(0));
    assertThat(sourceLink.get("id")).isEqualTo(source.id().toString());
    assertThat(sourceLink.get("rationale")).isEqualTo(source.rationale());
    assertThat(map(sourceLink.get("problem")).get("id")).isEqualTo(problem.id().toString());
    assertThat(map(map(list(fetchedSignal.get("solutions")).get(0)).get("solution")).get("id"))
        .isEqualTo(solution.id().toString());
    var fetchedProblem = map(data.get("problem"));
    var fetchedQuestion = map(list(fetchedProblem.get("questions")).get(0));
    assertThat(fetchedQuestion.get("id")).isEqualTo(question.id().toString());
    assertThat(fetchedQuestion.get("status")).isEqualTo("ANSWERED");
    assertThat(fetchedQuestion.get("answer")).isEqualTo(question.answer());
    assertThat(map(map(list(fetchedProblem.get("signals")).get(0)).get("signal")).get("content"))
        .isEqualTo(signal.content());
    assertThat(
            map(map(list(fetchedProblem.get("solutions")).get(0)).get("solution")).get("approach"))
        .isEqualTo(solution.approach());
    var fetchedSolution = map(data.get("solution"));
    var fetchedDecision = map(list(fetchedSolution.get("decisions")).get(0));
    assertThat(fetchedDecision.get("id")).isEqualTo(decision.id().toString());
    assertThat(fetchedDecision.get("source")).isEqualTo(decision.source());
    assertThat(map(map(list(fetchedSolution.get("problems")).get(0)).get("problem")).get("id"))
        .isEqualTo(problem.id().toString());
  }

  @Test
  void query_listsNewestFirstWithoutHidingUnlinkedDrafts() {
    var older = intake.capture(randomString(), randomString(), null);
    var newer = intake.capture(randomString(), randomString(), null);
    var orphan = refinement.createSolution(randomString(), randomString(), null, null, null);

    var result =
        query(
            """
            { first: signals(limit: 1) { id }
              second: signals(limit: 1, page: 1) { id }
              solutions { id problems { id } }
              strategy { content } }
            """,
            Map.of());

    assertThat(result.get("errors")).isNull();
    var data = map(result.get("data"));
    assertThat(map(list(data.get("first")).get(0)).get("id")).isEqualTo(newer.id().toString());
    assertThat(map(list(data.get("second")).get(0)).get("id")).isEqualTo(older.id().toString());
    var candidate = map(list(data.get("solutions")).get(0));
    assertThat(candidate.get("id")).isEqualTo(orphan.id().toString());
    assertThat(list(candidate.get("problems"))).isEmpty();
    assertThat(map(data.get("strategy")).get("content")).isNull();
  }

  @Test
  void query_returnsErrorsForInvalidRequestsAndOffersNoMutations() {
    for (var operation :
        List.of(
            "{ signals(limit: 101) { id } }",
            "{ problems(page: -1) { id } }",
            "{ signal(id: \"bad-id\") { id } }",
            "{ problem(id: \"" + UUID.randomUUID() + "\") { id } }",
            "mutation { wide_capture_signal(title: \"bad\", content: \"bad\") { id } }")) {
      assertThat(query(operation, Map.of()).get("errors")).as(operation).isNotNull();
    }
    assertThat(signals.count()).isZero();
    assertThat(problems.count()).isZero();
  }

  private Map<?, ?> query(String query, Map<String, ?> variables) {
    return RestClient.create(baseUrl())
        .post()
        .uri("/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("query", query, "variables", variables))
        .retrieve()
        .body(Map.class);
  }

  private static Map<?, ?> map(Object value) {
    return (Map<?, ?>) value;
  }

  private static List<?> list(Object value) {
    return (List<?>) value;
  }
}
