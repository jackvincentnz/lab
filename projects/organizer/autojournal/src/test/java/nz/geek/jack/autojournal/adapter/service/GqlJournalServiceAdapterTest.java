package nz.geek.jack.autojournal.adapter.service;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
class GqlJournalServiceAdapterTest {

  @BeforeEach
  void beforeEach() {
    stubFor(post("/graphql").willReturn(ok().withBody("{}")));
  }

  @Test
  void addEntry_addsEntryMatchingMessage(WireMockRuntimeInfo wmRuntimeInfo) {
    var gqlJournalServiceAdapter = newAdapter(wmRuntimeInfo);
    var message = "My message";

    gqlJournalServiceAdapter.addEntry(message);

    verify(
        postRequestedFor(urlEqualTo("/graphql"))
            .withRequestBody(containing("addEntry(input: {message : \\\"" + message + "\\\"})")));
  }

  private GqlJournalServiceAdapter newAdapter(WireMockRuntimeInfo wmRuntimeInfo) {
    var gqlUrl = String.format("http://localhost:%s/graphql", wmRuntimeInfo.getHttpPort());
    var properties = new JournalServiceProperties();
    properties.setGraphqlUrl(gqlUrl);
    return new GqlJournalServiceAdapter(properties);
  }
}
