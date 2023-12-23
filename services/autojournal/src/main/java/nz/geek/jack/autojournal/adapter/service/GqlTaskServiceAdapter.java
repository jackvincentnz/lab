package nz.geek.jack.autojournal.adapter.service;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.List;
import nz.geek.jack.autojournal.application.Task;
import nz.geek.jack.autojournal.application.TaskService;
import nz.geek.jack.task.adapter.gql.schema.client.AllTasksGraphQLQuery;
import nz.geek.jack.task.adapter.gql.schema.client.AllTasksProjectionRoot;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GqlTaskServiceAdapter implements TaskService {

  private final WebClientGraphQLClient client;

  public GqlTaskServiceAdapter(TaskServiceProperties taskServiceProperties) {
    var webClient = WebClient.create(taskServiceProperties.getGraphqlUrl());
    client = MonoGraphQLClient.createWithWebClient(webClient);
  }

  @Override
  public Task getTask(String taskId) {
    var query =
        new GraphQLQueryRequest(
            AllTasksGraphQLQuery.newRequest().build(), new AllTasksProjectionRoot().id().title());

    var response = client.reactiveExecuteQuery(query.serialize()).block();
    var tasks =
        response.extractValueAsObject(
            "data.allTasks[*]",
            new TypeRef<List<nz.geek.jack.autojournal.adapter.service.Task>>() {});

    var task = tasks.stream().filter(t -> t.id().equals(taskId)).findFirst().get();

    return new Task(taskId, task.title());
  }
}
