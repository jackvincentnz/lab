package nz.geek.jack.autojournal.infrastructure.service;

import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import nz.geek.jack.autojournal.application.JournalService;
import nz.geek.jack.journal.infrastructure.gql.schema.client.AddEntryGraphQLQuery;
import nz.geek.jack.journal.infrastructure.gql.schema.client.AddEntryProjectionRoot;
import nz.geek.jack.journal.infrastructure.gql.schema.types.AddEntryInput;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/*
 * Based on examples here: https://netflix.github.io/dgs/advanced/java-client/
 */
@Service
public class GqlJournalServiceAdapter implements JournalService {

  private final WebClientGraphQLClient client;

  private GqlJournalServiceAdapter(JournalServiceProperties journalServiceProperties) {
    var webClient = WebClient.create(journalServiceProperties.getGraphqlUrl());
    client = MonoGraphQLClient.createWithWebClient(webClient);
  }

  @Override
  public void addEntry(String message) {
    var request =
        new GraphQLQueryRequest(
            AddEntryGraphQLQuery.newRequest()
                .input(AddEntryInput.newBuilder().message(message).build())
                .build(),
            new AddEntryProjectionRoot().id());

    // TODO: block and wait for response to ensure transactional consume of TaskCompletedEvent
    // TODO: handle errors
    client.reactiveExecuteQuery(request.serialize()).subscribe();
  }
}
