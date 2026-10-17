package lab.autojournal.adapter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class GqlJournalServiceAdapterTest extends TestBase {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void addEntry_addsEntryMatchingMessage() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var gqlJournalServiceAdapter = newAdapter(server);
      var message = randomString();
      server.respondWith(addEntryResponse(randomId()));

      gqlJournalServiceAdapter.addEntry(message);

      assertThat(server.requestBody())
          .contains("addEntry(input: {message : \\\"" + message + "\\\"})");
    }
  }

  @Test
  void addEntry_responseHasErrors_throwsWithErrorMessage() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var gqlJournalServiceAdapter = newAdapter(server);
      var errorMessage = randomString();
      var responseJson = objectMapper.createObjectNode();
      responseJson.putArray("errors").addObject().put("message", errorMessage);
      server.respondWith(responseJson.toString());

      assertThatThrownBy(() -> gqlJournalServiceAdapter.addEntry(randomString()))
          .isInstanceOf(GraphqlServiceException.class)
          .hasMessageContaining(errorMessage);
    }
  }

  @Test
  void addEntry_non2xxStatus_throws() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var gqlJournalServiceAdapter = newAdapter(server);
      server.respondWith(500, addEntryResponse(randomId()));

      assertThatThrownBy(() -> gqlJournalServiceAdapter.addEntry(randomString()))
          .isInstanceOf(GraphqlServiceException.class)
          .hasMessageContaining("GraphQL request failed");
    }
  }

  @Test
  void addEntry_responseMissingData_throws() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var gqlJournalServiceAdapter = newAdapter(server);
      server.respondWith("{}");

      assertThatThrownBy(() -> gqlJournalServiceAdapter.addEntry(randomString()))
          .isInstanceOf(GraphqlServiceException.class)
          .hasMessage("GraphQL request returned no data");
    }
  }

  private String addEntryResponse(String entryId) {
    var responseJson = objectMapper.createObjectNode();
    responseJson.putObject("data").putObject("addEntry").put("id", entryId);
    return responseJson.toString();
  }

  private GqlJournalServiceAdapter newAdapter(RecordingGraphqlServer server) {
    var properties = new JournalServiceProperties();
    properties.setGraphqlUrl(server.graphqlUrl());
    return new GqlJournalServiceAdapter(properties);
  }
}
