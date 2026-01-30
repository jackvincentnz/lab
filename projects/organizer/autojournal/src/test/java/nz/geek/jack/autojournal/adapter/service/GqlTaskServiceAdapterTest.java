package nz.geek.jack.autojournal.adapter.service;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@WireMockTest
class GqlTaskServiceAdapterTest {

  static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void getTask_returnsTask(WireMockRuntimeInfo wmRuntimeInfo) {
    var taskServiceAdapter = newAdapter(wmRuntimeInfo);

    var taskId = UUID.randomUUID().toString();
    var taskTitle = "My task";

    var responseJson = OBJECT_MAPPER.createObjectNode();
    responseJson.putObject("data").putObject("task").put("id", taskId).put("title", taskTitle);

    // TODO: verify that post body contains expected query
    stubFor(post("/graphql").willReturn(ok().withBody(responseJson.toString())));

    var task = taskServiceAdapter.getTask(taskId);

    assertThat(task.taskId()).isEqualTo(taskId);
    assertThat(task.title()).isEqualTo(taskTitle);
  }

  private GqlTaskServiceAdapter newAdapter(WireMockRuntimeInfo wmRuntimeInfo) {
    var gqlUrl = String.format("http://localhost:%s/graphql", wmRuntimeInfo.getHttpPort());
    var properties = new TaskServiceProperties();
    properties.setGraphqlUrl(gqlUrl);
    return new GqlTaskServiceAdapter(properties);
  }
}
