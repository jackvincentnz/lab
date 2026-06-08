package lab.autojournal.adapter.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GqlJournalServiceAdapterTest {

  @Test
  void addEntry_addsEntryMatchingMessage() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var gqlJournalServiceAdapter = newAdapter(server);
      var message = "My message";

      gqlJournalServiceAdapter.addEntry(message);

      assertThat(server.requestBody())
          .contains("addEntry(input: {message : \\\"" + message + "\\\"})");
    }
  }

  private GqlJournalServiceAdapter newAdapter(RecordingGraphqlServer server) {
    var properties = new JournalServiceProperties();
    properties.setGraphqlUrl(server.graphqlUrl());
    return new GqlJournalServiceAdapter(properties);
  }
}
