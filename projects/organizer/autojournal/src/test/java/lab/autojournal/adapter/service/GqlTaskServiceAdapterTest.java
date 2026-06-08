package lab.autojournal.adapter.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GqlTaskServiceAdapterTest {

  static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void getTask_returnsTask() throws Exception {
    try (var server = new RecordingGraphqlServer()) {
      var taskServiceAdapter = newAdapter(server);

      var taskId = UUID.randomUUID().toString();
      var taskTitle = "My task";

      var responseJson = OBJECT_MAPPER.createObjectNode();
      responseJson.putObject("data").putObject("task").put("id", taskId).put("title", taskTitle);

      server.respondWith(responseJson.toString());

      var task = taskServiceAdapter.getTask(taskId);

      assertThat(task.taskId()).isEqualTo(taskId);
      assertThat(task.title()).isEqualTo(taskTitle);
      assertThat(server.requestBody()).contains(taskId);
    }
  }

  private GqlTaskServiceAdapter newAdapter(RecordingGraphqlServer server) {
    var properties = new TaskServiceProperties();
    properties.setGraphqlUrl(server.graphqlUrl());
    return new GqlTaskServiceAdapter(properties);
  }
}
