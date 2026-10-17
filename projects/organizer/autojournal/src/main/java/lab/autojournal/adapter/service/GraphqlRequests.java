package lab.autojournal.adapter.service;

import com.netflix.graphql.dgs.client.GraphQLError;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import java.util.stream.Collectors;

final class GraphqlRequests {

  private GraphqlRequests() {}

  /**
   * Executes the query and returns a response whose {@code data.<field>} is not null.
   *
   * <p>The DGS client returns GraphQL errors, a missing {@code data} field, and a null field as an
   * ordinary response, so callers would otherwise treat a failed request as success.
   */
  static GraphQLResponse execute(WebClientGraphQLClient client, String query, String field) {
    return requireField(execute(client, query), field, "");
  }

  /** Like {@link #execute(WebClientGraphQLClient, String, String)}, naming the requested id. */
  static GraphQLResponse execute(
      WebClientGraphQLClient client, String query, String field, String id) {
    return requireField(execute(client, query), field, " for id " + id);
  }

  private static GraphQLResponse requireField(
      GraphQLResponse response, String field, String context) {
    if (response.getData().get(field) == null) {
      throw new GraphqlServiceException("GraphQL request returned null data." + field + context);
    }
    return response;
  }

  private static GraphQLResponse execute(WebClientGraphQLClient client, String query) {
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
