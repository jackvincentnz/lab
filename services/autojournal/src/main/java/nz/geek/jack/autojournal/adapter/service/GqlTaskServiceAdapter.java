package nz.geek.jack.autojournal.adapter.service;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import nz.geek.jack.autojournal.application.Task;
import nz.geek.jack.autojournal.application.TaskService;
import nz.geek.jack.task.adapter.gql.schema.client.TaskGraphQLQuery;
import nz.geek.jack.task.adapter.gql.schema.client.TaskProjectionRoot;
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
            TaskGraphQLQuery.newRequest().id(taskId).build(),
            new TaskProjectionRoot<>().id().title());

    var response = client.reactiveExecuteQuery(query.serialize()).block();
    var task =
        response.extractValueAsObject(
            "data.task", new TypeRef<nz.geek.jack.autojournal.adapter.service.Task>() {});

    return new Task(taskId, task.title());
  }
}
