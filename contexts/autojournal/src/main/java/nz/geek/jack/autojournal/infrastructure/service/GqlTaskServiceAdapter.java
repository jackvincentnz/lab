package nz.geek.jack.autojournal.infrastructure.service;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import java.util.List;
import nz.geek.jack.autojournal.application.Task;
import nz.geek.jack.autojournal.application.TaskService;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GqlTaskServiceAdapter implements TaskService {

  private final WebClientGraphQLClient client;

  private GqlTaskServiceAdapter(TaskServiceProperties taskServiceProperties) {
    var webClient = WebClient.create(taskServiceProperties.getGraphqlUrl());
    client = MonoGraphQLClient.createWithWebClient(webClient);
  }

  @Override
  public Task getTask(String taskId) {
    // TODO: switch task context to dgs and add getTask query
    @Language("graphql")
    var query =
        """
      query {
        allTasks {
          id
          title
        }
      }
      """;

    var response = client.reactiveExecuteQuery(query).block();
    var tasks =
        response.extractValueAsObject(
            "data.allTasks[*]",
            new TypeRef<List<nz.geek.jack.autojournal.infrastructure.service.Task>>() {});

    var task = tasks.stream().filter(t -> t.id().equals(taskId)).findFirst().get();

    return new Task(taskId, task.title());
  }
}
