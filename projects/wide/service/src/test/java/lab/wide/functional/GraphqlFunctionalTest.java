package lab.wide.functional;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.ErrorType;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.List;
import java.util.UUID;
import lab.test.RequiresDocker;
import lab.wide.api.gql.client.ProblemGraphQLQuery;
import lab.wide.api.gql.client.ProblemProjectionRoot;
import lab.wide.api.gql.client.SignalGraphQLQuery;
import lab.wide.api.gql.client.SignalProjectionRoot;
import lab.wide.api.gql.client.SignalsGraphQLQuery;
import lab.wide.api.gql.client.SignalsProjectionRoot;
import lab.wide.api.gql.client.SolutionsGraphQLQuery;
import lab.wide.api.gql.client.SolutionsProjectionRoot;
import lab.wide.api.gql.client.StrategyGraphQLQuery;
import lab.wide.api.gql.client.StrategyProjectionRoot;
import lab.wide.api.gql.types.Problem;
import lab.wide.api.gql.types.Signal;
import lab.wide.api.gql.types.Solution;
import lab.wide.api.gql.types.Strategy;
import lab.wide.application.RefinementService;
import lab.wide.application.SignalService;
import lab.wide.domain.SignalRepository;
import lab.wide.testing.ServerTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@RequiresDocker
class GraphqlFunctionalTest extends ServerTestBase {

  @Autowired SignalService signalService;

  @Autowired RefinementService refinementService;

  @Autowired SignalRepository signalRepository;

  @Test
  void query_pagedSignals_returnsNewestFirst() {
    var older = signalService.capture(randomString(), randomString(), null);
    var newer = signalService.capture(randomString(), randomString(), null);

    var firstPage = graphql(signalIds(SignalsGraphQLQuery.newRequest().limit(1).build()));
    var secondPage = graphql(signalIds(SignalsGraphQLQuery.newRequest().limit(1).page(1).build()));

    assertThat(firstPage.hasErrors()).isFalse();
    assertThat(secondPage.hasErrors()).isFalse();
    assertThat(firstPage.extractValueAsObject("signals", new TypeRef<List<Signal>>() {}))
        .extracting(Signal::getId)
        .containsExactly(newer.id().toString());
    assertThat(secondPage.extractValueAsObject("signals", new TypeRef<List<Signal>>() {}))
        .extracting(Signal::getId)
        .containsExactly(older.id().toString());
  }

  @Test
  void query_unlinkedSolutionAndUnsetStrategy_returnsEmptyListAndNulls() {
    var solution =
        refinementService.createSolution(randomString(), randomString(), null, null, null);

    var solutionsResponse =
        graphql(
            new GraphQLQueryRequest(
                SolutionsGraphQLQuery.newRequest().build(),
                new SolutionsProjectionRoot<>().id().scope().problems().id()));
    var strategyResponse =
        graphql(
            new GraphQLQueryRequest(
                StrategyGraphQLQuery.newRequest().build(),
                new StrategyProjectionRoot<>().content().updatedAt()));

    var fetchedSolution =
        solutionsResponse
            .extractValueAsObject("solutions", new TypeRef<List<Solution>>() {})
            .get(0);
    var fetchedStrategy = strategyResponse.extractValueAsObject("strategy", Strategy.class);
    assertThat(solutionsResponse.hasErrors()).isFalse();
    assertThat(strategyResponse.hasErrors()).isFalse();
    assertThat(fetchedSolution.getId()).isEqualTo(solution.id().toString());
    assertThat(fetchedSolution.getScope()).isNull();
    assertThat(fetchedSolution.getProblems()).isEmpty();
    assertThat(fetchedStrategy.getContent()).isNull();
    assertThat(fetchedStrategy.getUpdatedAt()).isNull();
  }

  @Test
  void query_limitOutOfRange_returnsError() {
    var response = graphql(signalIds(SignalsGraphQLQuery.newRequest().limit(101).build()));

    assertThat(response.hasErrors()).isTrue();
  }

  @Test
  void query_malformedId_returnsBadRequest() {
    var response =
        graphql(
            new GraphQLQueryRequest(
                SignalGraphQLQuery.newRequest().id("bad-id").build(),
                new SignalProjectionRoot<>().id()));

    assertThat(response.getErrors().get(0).getExtensions().getErrorType())
        .isEqualTo(ErrorType.BAD_REQUEST);
  }

  @Test
  void query_unknownId_returnsNullWithoutErrors() {
    var response =
        graphql(
            new GraphQLQueryRequest(
                ProblemGraphQLQuery.newRequest().id(UUID.randomUUID().toString()).build(),
                new ProblemProjectionRoot<>().id()));

    assertThat(response.hasErrors()).isFalse();
    assertThat(response.extractValueAsObject("problem", Problem.class)).isNull();
  }

  @Test
  void mutation_anyOperation_isNotOfferedAndWritesNothing() {
    var response =
        graphql("mutation { wide_capture_signal(title: \"Demo\", content: \"Demo\") { id } }");

    assertThat(response.hasErrors()).isTrue();
    assertThat(signalRepository.count()).isZero();
  }

  private static GraphQLQueryRequest signalIds(SignalsGraphQLQuery signalsGraphQLQuery) {
    return new GraphQLQueryRequest(signalsGraphQLQuery, new SignalsProjectionRoot<>().id());
  }
}
