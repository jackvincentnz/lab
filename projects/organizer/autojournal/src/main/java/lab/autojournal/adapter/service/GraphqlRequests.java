package lab.autojournal.adapter.service;

import com.netflix.graphql.dgs.client.GraphQLError;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import java.util.stream.Collectors;

final class GraphqlRequests {

  private GraphqlRequests() {}

  /**
   * Executes the query and returns a response that carries data.
   *
   * <p>The DGS client returns GraphQL errors and a missing {@code data} field as an ordinary
   * response, so callers would otherwise treat a failed request as success.
   */
  static GraphQLResponse execute(WebClientGraphQLClient client, String query) {
    GraphQLResponse response;
    try {
      response = client.reactiveExecuteQuery(query).block();
    } catch (RuntimeException e) {
      throw new GraphqlServiceException("GraphQL request failed: " + e.getMessage(), e);
    }

    if (response == null) {
      throw new GraphqlServiceException("GraphQL request returned no response");
    }
    if (response.hasErrors()) {
      var messages =
          response.getErrors().stream()
              .map(GraphQLError::getMessage)
              .collect(Collectors.joining("; "));
      throw new GraphqlServiceException("GraphQL request returned errors: " + messages);
    }
    if (response.getData().isEmpty()) {
      throw new GraphqlServiceException("GraphQL request returned no data");
    }
    return response;
  }
}
