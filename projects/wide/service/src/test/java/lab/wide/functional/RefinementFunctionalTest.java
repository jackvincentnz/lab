package lab.wide.functional;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lab.test.RequiresDocker;
import lab.wide.api.RefinementDetailTools;
import lab.wide.api.RefinementTools;
import lab.wide.api.SearchTools;
import lab.wide.api.SignalTools;
import lab.wide.api.StrategyTools;
import lab.wide.api.gql.client.ProblemGraphQLQuery;
import lab.wide.api.gql.client.ProblemProjectionRoot;
import lab.wide.api.gql.client.SignalGraphQLQuery;
import lab.wide.api.gql.client.SignalProjectionRoot;
import lab.wide.api.gql.client.SignalsGraphQLQuery;
import lab.wide.api.gql.client.SignalsProjectionRoot;
import lab.wide.api.gql.client.SolutionGraphQLQuery;
import lab.wide.api.gql.client.SolutionProjectionRoot;
import lab.wide.api.gql.types.Problem;
import lab.wide.api.gql.types.QuestionStatus;
import lab.wide.api.gql.types.Signal;
import lab.wide.api.gql.types.Solution;
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
        callTool(RefinementTools.GET_PROBLEM, Map.of("id", refinement.problemId().toString()));

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
        callTool(RefinementTools.GET_SOLUTION, Map.of("id", refinement.solutionId().toString()));

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

    var signal = callTool(SignalTools.GET_SIGNAL, Map.of("id", refinement.signalId()));
    var strategy = callTool(StrategyTools.READ_STRATEGY, Map.of());
    var page = callTool(SearchTools.SEARCH, Map.of("query", refinement.problemTitle()));

    assertThat(text(signal)).contains(refinement.signalTitle());
    assertThat(text(strategy)).contains(refinement.strategy());
    assertThat(text(page))
        .contains(refinement.problemId().toString())
        .doesNotContain(refinement.strayProblemId().toString());
  }

  @Test
  void graphqlQuery_afterRefinementOverMcp_readsLinkedContextInBothDirections() {
    var refinement = refine();

    var signalsResponse = graphql(signalsTitled(refinement.signalTitle()));
    var signalResponse = graphql(signalWithLinks(refinement.signalId()));
    var problemResponse = graphql(problemWithContext(refinement.problemId()));
    var solutionResponse = graphql(solutionWithContext(refinement.solutionId()));

    var signals = signalsResponse.extractValueAsObject("signals", new TypeRef<List<Signal>>() {});
    var signal = signalResponse.extractValueAsObject("signal", Signal.class);
    var problem = problemResponse.extractValueAsObject("problem", Problem.class);
    var solution = solutionResponse.extractValueAsObject("solution", Solution.class);
    var question = problem.getQuestions().get(0);
    assertThat(List.of(signalsResponse, signalResponse, problemResponse, solutionResponse))
        .noneMatch(GraphQLResponse::hasErrors);
    assertThat(signals)
        .singleElement()
        .extracting(Signal::getId)
        .isEqualTo(refinement.signalId().toString());
    assertThat(signal.getCapturedAt())
        .isEqualTo(signalService.get(refinement.signalId()).capturedAt().toString());
    assertThat(signal.getProblems().get(0).getProblem().getId())
        .isEqualTo(refinement.problemId().toString());
    assertThat(signal.getSolutions().get(0).getSolution().getId())
        .isEqualTo(refinement.solutionId().toString());
    assertThat(question.getQuestion()).isEqualTo(refinement.question());
    assertThat(question.getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    assertThat(question.getAnswer()).isEqualTo(refinement.answer());
    assertThat(problem.getSignals().get(0).getSignal().getId())
        .isEqualTo(refinement.signalId().toString());
    assertThat(problem.getSolutions().get(0).getSolution().getTitle())
        .isEqualTo(refinement.solutionTitle());
    assertThat(solution.getDecisions().get(0).getDecision()).isEqualTo(refinement.decision());
    assertThat(solution.getProblems().get(0).getProblem().getTitle())
        .isEqualTo(refinement.problemTitle());
  }

  private Refinement refine() {
    var draftStrategy = randomString();
    var strategy = randomString();
    write(StrategyTools.REPLACE_STRATEGY, Map.of("content", draftStrategy));
    write(StrategyTools.EDIT_STRATEGY, Map.of("oldText", draftStrategy, "newText", strategy));

    var signalTitle = randomString();
    var signalId =
        write(SignalTools.CAPTURE_SIGNAL, Map.of("title", signalTitle, "content", randomString()));

    var problemTitle = randomString();
    var problemId =
        write(
            RefinementTools.CREATE_PROBLEM,
            Map.of(
                "title",
                randomString(),
                "description",
                randomString(),
                "signalLinks",
                List.of(link("signalId", signalId))));
    write(
        RefinementTools.EDIT_PROBLEM,
        Map.of("id", problemId, "fields", Map.of("title", problemTitle)));

    var solutionTitle = randomString();
    var solutionId =
        write(
            RefinementTools.CREATE_SOLUTION,
            Map.of(
                "title",
                randomString(),
                "approach",
                randomString(),
                "signalLinks",
                List.of(link("signalId", signalId)),
                "problemLinks",
                List.of(link("problemId", problemId))));
    write(
        RefinementTools.EDIT_SOLUTION,
        Map.of("id", solutionId, "fields", Map.of("title", solutionTitle)));

    var straySignalId =
        write(
            SignalTools.CAPTURE_SIGNAL, Map.of("title", randomString(), "content", randomString()));
    var strayProblemId =
        write(
            RefinementTools.CREATE_PROBLEM,
            Map.of("title", randomString(), "description", randomString()));
    linkThenUnlink(
        RefinementTools.LINK_PROBLEM_SIGNAL,
        RefinementTools.UNLINK_PROBLEM_SIGNAL,
        Map.of("problemId", problemId, "signalId", straySignalId));
    linkThenUnlink(
        RefinementTools.LINK_SOLUTION_SIGNAL,
        RefinementTools.UNLINK_SOLUTION_SIGNAL,
        Map.of("solutionId", solutionId, "signalId", straySignalId));
    linkThenUnlink(
        RefinementTools.LINK_SOLUTION_PROBLEM,
        RefinementTools.UNLINK_SOLUTION_PROBLEM,
        Map.of("solutionId", solutionId, "problemId", strayProblemId));

    var question = randomString();
    var answer = randomString();
    var questionId =
        write(
            RefinementDetailTools.CREATE_QUESTION,
            Map.of("problemId", problemId, "question", question, "status", "OPEN"));
    write(
        RefinementDetailTools.EDIT_QUESTION,
        Map.of("id", questionId, "fields", Map.of("status", "ANSWERED", "answer", answer)));

    var decision = randomString();
    var decisionId =
        write(
            RefinementDetailTools.CREATE_DECISION,
            Map.of("solutionId", solutionId, "decision", randomString()));
    write(
        RefinementDetailTools.EDIT_DECISION,
        Map.of("id", decisionId, "fields", Map.of("decision", decision)));

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

  private void linkThenUnlink(String linkTool, String unlinkTool, Map<String, Object> endpoints) {
    var arguments = new HashMap<>(endpoints);
    arguments.put("rationale", randomString());
    var linkId = write(linkTool, arguments);

    write(unlinkTool, Map.of("id", linkId));
  }

  private Map<String, Object> link(String idField, UUID id) {
    return Map.of(idField, id, "rationale", randomString());
  }

  private static GraphQLQueryRequest signalsTitled(String title) {
    return new GraphQLQueryRequest(
        SignalsGraphQLQuery.newRequest().query(title).build(), new SignalsProjectionRoot<>().id());
  }

  private static GraphQLQueryRequest signalWithLinks(UUID id) {
    return new GraphQLQueryRequest(
        SignalGraphQLQuery.newRequest().id(id.toString()).build(),
        new SignalProjectionRoot<>()
            .capturedAt()
            .problems()
            .problem()
            .id()
            .parent()
            .parent()
            .solutions()
            .solution()
            .id());
  }

  private static GraphQLQueryRequest problemWithContext(UUID id) {
    return new GraphQLQueryRequest(
        ProblemGraphQLQuery.newRequest().id(id.toString()).build(),
        new ProblemProjectionRoot<>()
            .questions()
            .question()
            .status()
            .parent()
            .answer()
            .parent()
            .signals()
            .signal()
            .id()
            .parent()
            .parent()
            .solutions()
            .solution()
            .title());
  }

  private static GraphQLQueryRequest solutionWithContext(UUID id) {
    return new GraphQLQueryRequest(
        SolutionGraphQLQuery.newRequest().id(id.toString()).build(),
        new SolutionProjectionRoot<>()
            .decisions()
            .decision()
            .parent()
            .problems()
            .problem()
            .title());
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
