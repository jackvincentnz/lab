package lab.wide.functional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;
import lab.test.RequiresDocker;
import lab.wide.application.RefinementService;
import lab.wide.application.SignalService;
import lab.wide.infrastructure.SignalRepository;
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

    var response =
        graphql(
            "{ first: signals(limit: 1) { id } second: signals(limit: 1, page: 1) { id } }",
            Map.of());

    var data = map(response.get("data"));
    assertThat(response.get("errors")).isNull();
    assertThat(map(list(data.get("first")).get(0)).get("id")).isEqualTo(newer.id().toString());
    assertThat(map(list(data.get("second")).get(0)).get("id")).isEqualTo(older.id().toString());
  }

  @Test
  void query_unlinkedSolutionAndUnsetStrategy_returnsEmptyListAndNulls() {
    var solution =
        refinementService.createSolution(randomString(), randomString(), null, null, null);

    var response =
        graphql(
            "{ solutions { id scope problems { id } } strategy { content updatedAt } }", Map.of());

    var data = map(response.get("data"));
    var fetchedSolution = map(list(data.get("solutions")).get(0));
    var fetchedStrategy = map(data.get("strategy"));
    assertThat(response.get("errors")).isNull();
    assertThat(fetchedSolution.get("id")).isEqualTo(solution.id().toString());
    assertThat(fetchedSolution.get("scope")).isNull();
    assertThat(list(fetchedSolution.get("problems"))).isEmpty();
    assertThat(fetchedStrategy.get("content")).isNull();
    assertThat(fetchedStrategy.get("updatedAt")).isNull();
  }

  @Test
  void query_limitOutOfRange_returnsError() {
    var response = graphql("{ signals(limit: 101) { id } }", Map.of());

    assertThat(response.get("errors")).isNotNull();
  }

  @Test
  void query_malformedId_returnsBadRequest() {
    var response = graphql("{ signal(id: \"bad-id\") { id } }", Map.of());

    var error = map(list(response.get("errors")).get(0));
    assertThat(map(error.get("extensions")).get("errorType")).isEqualTo("BAD_REQUEST");
  }

  @Test
  void query_unknownId_returnsNullWithoutErrors() {
    var response = graphql("{ problem(id: \"" + UUID.randomUUID() + "\") { id } }", Map.of());

    assertThat(response.get("errors")).isNull();
    assertThat(map(response.get("data")).get("problem")).isNull();
  }

  @Test
  void mutation_anyOperation_isNotOfferedAndWritesNothing() {
    var response =
        graphql(
            "mutation { wide_capture_signal(title: \"Demo\", content: \"Demo\") { id } }",
            Map.of());

    assertThat(response.get("errors")).isNotNull();
    assertThat(signalRepository.count()).isZero();
  }
}
