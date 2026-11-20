package lab.wide.functional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.test.RequiresDocker;
import lab.wide.application.SignalService;
import lab.wide.testing.ServerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Records a refined problem and candidate over MCP, then reads them back as each client does. */
@RequiresDocker
class RefinementFunctionalTest extends ServerTestBase {

  @Autowired SignalService signalService;

  @Test
  void getProblem_afterRefinementOverMcp_returnsLinksQuestionsAndLinkedTitles() {
    var refinement = refine();

    var callToolResult =
        callTool("wide_get_problem", Map.of("id", refinement.problemId().toString()));

    assertThat(callToolResult.isError()).isNotEqualTo(true);
    assertThat(text(callToolResult))
        .contains(
            refinement.problemTitle(),
            refinement.question(),
            refinement.answer(),
            "\"" + refinement.signalId() + "\":\"" + refinement.signalTitle() + "\"",
            "\"" + refinement.solutionId() + "\":\"" + refinement.solutionTitle() + "\"")
        .doesNotContain(refinement.straySignalId().toString());
  }

  @Test
  void getSolution_afterRefinementOverMcp_returnsLinksAndDecisions() {
    var refinement = refine();

    var callToolResult =
        callTool("wide_get_solution", Map.of("id", refinement.solutionId().toString()));

    assertThat(callToolResult.isError()).isNotEqualTo(true);
    assertThat(text(callToolResult))
        .contains(
            refinement.solutionTitle(),
            refinement.decision(),
            refinement.problemId().toString(),
            refinement.signalId().toString())
        .doesNotContain(
            refinement.straySignalId().toString(), refinement.strayProblemId().toString());
  }

  @Test
  void readTools_afterRefinementOverMcp_returnTheSignalStrategyAndSearchMatch() {
    var refinement = refine();

    var signal = callTool("wide_get_signal", Map.of("id", refinement.signalId()));
    var strategy = callTool("wide_read_strategy", Map.of());
    var page = callTool("wide_search", Map.of("query", refinement.problemTitle()));

    assertThat(text(signal)).contains(refinement.signalTitle());
    assertThat(text(strategy)).contains(refinement.strategy());
    assertThat(text(page))
        .contains(refinement.problemId().toString())
        .doesNotContain(refinement.strayProblemId().toString());
  }

  @Test
  void graphqlQuery_afterRefinementOverMcp_readsLinkedContextInBothDirections() {
    var refinement = refine();

    var response =
        graphql(
            """
            query Context($signalTitle: String!, $signal: ID!, $problem: ID!, $solution: ID!) {
              signals(query: $signalTitle) { id }
              signal(id: $signal) {
                capturedAt
                problems { problem { id } }
                solutions { solution { id } }
              }
              problem(id: $problem) {
                questions { question status answer }
                signals { signal { id } }
                solutions { solution { title } }
              }
              solution(id: $solution) {
                decisions { decision }
                problems { problem { title } }
              }
            }
            """,
            Map.of(
                "signalTitle", refinement.signalTitle(),
                "signal", refinement.signalId(),
                "problem", refinement.problemId(),
                "solution", refinement.solutionId()));

    var data = map(response.get("data"));
    var signal = map(data.get("signal"));
    var problem = map(data.get("problem"));
    var solution = map(data.get("solution"));
    var question = map(list(problem.get("questions")).get(0));
    assertThat(response.get("errors")).isNull();
    assertThat(list(data.get("signals")))
        .singleElement()
        .isEqualTo(Map.of("id", refinement.signalId().toString()));
    assertThat(signal.get("capturedAt"))
        .isEqualTo(signalService.get(refinement.signalId()).capturedAt().toString());
    assertThat(linked(signal, "problems", "problem", "id"))
        .isEqualTo(refinement.problemId().toString());
    assertThat(linked(signal, "solutions", "solution", "id"))
        .isEqualTo(refinement.solutionId().toString());
    assertThat(question.get("question")).isEqualTo(refinement.question());
    assertThat(question.get("status")).isEqualTo("ANSWERED");
    assertThat(question.get("answer")).isEqualTo(refinement.answer());
    assertThat(linked(problem, "signals", "signal", "id"))
        .isEqualTo(refinement.signalId().toString());
    assertThat(linked(problem, "solutions", "solution", "title"))
        .isEqualTo(refinement.solutionTitle());
    assertThat(map(list(solution.get("decisions")).get(0)).get("decision"))
        .isEqualTo(refinement.decision());
    assertThat(linked(solution, "problems", "problem", "title"))
        .isEqualTo(refinement.problemTitle());
  }

  private Refinement refine() {
    var draftStrategy = randomString();
    var strategy = randomString();
    write("wide_replace_strategy", Map.of("content", draftStrategy));
    write("wide_edit_strategy", Map.of("oldText", draftStrategy, "newText", strategy));

    var signalTitle = randomString();
    var signalId =
        write("wide_capture_signal", Map.of("title", signalTitle, "content", randomString()));

    var problemTitle = randomString();
    var problemId =
        write(
            "wide_create_problem",
            Map.of(
                "title",
                randomString(),
                "description",
                randomString(),
                "signalLinks",
                List.of(link("signalId", signalId))));
    write("wide_edit_problem", Map.of("id", problemId, "fields", Map.of("title", problemTitle)));

    var solutionTitle = randomString();
    var solutionId =
        write(
            "wide_create_solution",
            Map.of(
                "title",
                randomString(),
                "approach",
                randomString(),
                "signalLinks",
                List.of(link("signalId", signalId)),
                "problemLinks",
                List.of(link("problemId", problemId))));
    write("wide_edit_solution", Map.of("id", solutionId, "fields", Map.of("title", solutionTitle)));

    var straySignalId =
        write("wide_capture_signal", Map.of("title", randomString(), "content", randomString()));
    var strayProblemId =
        write(
            "wide_create_problem", Map.of("title", randomString(), "description", randomString()));
    linkThenUnlink("problem_signal", Map.of("problemId", problemId, "signalId", straySignalId));
    linkThenUnlink("solution_signal", Map.of("solutionId", solutionId, "signalId", straySignalId));
    linkThenUnlink(
        "solution_problem", Map.of("solutionId", solutionId, "problemId", strayProblemId));

    var question = randomString();
    var answer = randomString();
    var questionId =
        write(
            "wide_create_question",
            Map.of("problemId", problemId, "question", question, "status", "OPEN"));
    write(
        "wide_edit_question",
        Map.of("id", questionId, "fields", Map.of("status", "ANSWERED", "answer", answer)));

    var decision = randomString();
    var decisionId =
        write("wide_create_decision", Map.of("solutionId", solutionId, "decision", randomString()));
    write("wide_edit_decision", Map.of("id", decisionId, "fields", Map.of("decision", decision)));

    return new Refinement(
        strategy,
        signalId,
        signalTitle,
        problemId,
        problemTitle,
        solutionId,
        solutionTitle,
        question,
        answer,
        decision,
        straySignalId,
        strayProblemId);
  }

  private UUID write(String tool, Map<String, Object> arguments) {
    return receiptId(callTool(tool, arguments));
  }

  private void linkThenUnlink(String link, Map<String, Object> endpoints) {
    var arguments = new HashMap<>(endpoints);
    arguments.put("rationale", randomString());
    var linkId = write("wide_link_" + link, arguments);

    write("wide_unlink_" + link, Map.of("id", linkId));
  }

  private Map<String, Object> link(String idField, UUID id) {
    return Map.of(idField, id, "rationale", randomString());
  }

  private static Object linked(Map<?, ?> parent, String links, String endpoint, String field) {
    return map(map(list(parent.get(links)).get(0)).get(endpoint)).get(field);
  }

  private record Refinement(
      String strategy,
      UUID signalId,
      String signalTitle,
      UUID problemId,
      String problemTitle,
      UUID solutionId,
      String solutionTitle,
      String question,
      String answer,
      String decision,
      UUID straySignalId,
      UUID strayProblemId) {}
}
