package lab.autojournal.adapter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class GqlTaskServiceAdapterTest extends TestBase {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void getTask_returnsTask() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var taskServiceAdapter = newAdapter(server);
      var taskId = randomId();
      var taskTitle = randomString();
      server.respondWith(taskResponse(taskId, taskTitle));

      var task = taskServiceAdapter.getTask(taskId);

      assertThat(task.taskId()).isEqualTo(taskId);
      assertThat(task.title()).isEqualTo(taskTitle);
      assertThat(server.requestBody()).contains(taskId);
    }
  }

  @Test
  void getTask_responseHasErrors_throwsWithErrorMessage() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var taskServiceAdapter = newAdapter(server);
      var errorMessage = randomString();
      var responseJson = objectMapper.createObjectNode();
      responseJson.putArray("errors").addObject().put("message", errorMessage);
      server.respondWith(responseJson.toString());

      assertThatThrownBy(() -> taskServiceAdapter.getTask(randomId()))
          .isInstanceOf(GraphqlServiceException.class)
          .hasMessageContaining(errorMessage);
    }
  }

  @Test
  void getTask_non2xxStatus_throws() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var taskServiceAdapter = newAdapter(server);
      var taskId = randomId();
      server.respondWith(500, taskResponse(taskId, randomString()));

      assertThatThrownBy(() -> taskServiceAdapter.getTask(taskId))
          .isInstanceOf(GraphqlServiceException.class)
          .hasMessageContaining("GraphQL request failed");
    }
  }

  @Test
  void getTask_responseMissingData_throws() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var taskServiceAdapter = newAdapter(server);
      server.respondWith("{}");

      assertThatThrownBy(() -> taskServiceAdapter.getTask(randomId()))
          .isInstanceOf(GraphqlServiceException.class)
          .hasMessage("GraphQL request returned no data");
    }
  }

  private String taskResponse(String taskId, String taskTitle) {
    var responseJson = objectMapper.createObjectNode();
    responseJson.putObject("data").putObject("task").put("id", taskId).put("title", taskTitle);
    return responseJson.toString();
  }

  private GqlTaskServiceAdapter newAdapter(RecordingGraphqlServer server) {
    var properties = new TaskServiceProperties();
    properties.setGraphqlUrl(server.graphqlUrl());
    return new GqlTaskServiceAdapter(properties);
  }
}
