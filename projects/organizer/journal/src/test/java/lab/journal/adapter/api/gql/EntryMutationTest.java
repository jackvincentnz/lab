package lab.journal.adapter.api.gql;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.netflix.graphql.dgs.test.EnableDgsTest;
import lab.journal.adapter.gql.schema.client.AddEntryGraphQLQuery;
import lab.journal.adapter.gql.schema.client.AddEntryProjectionRoot;
import lab.journal.adapter.gql.schema.types.AddEntryInput;
import lab.journal.application.entry.EntryCommandService;
import lab.journal.application.entry.EntryQueryService;
import lab.journal.config.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = {
      EntryMutation.class,
      EntryCommandService.class,
      EntryQueryService.class,
      EntryMapper.class,
      DatabaseConfig.class
    })
@EnableDgsTest
class EntryMutationTest {

  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @Test
  void addEntry_addsAndReturnsNewEntry() {
    var message = "New entry";
    var request =
        new GraphQLQueryRequest(
            AddEntryGraphQLQuery.newRequest()
                .input(AddEntryInput.newBuilder().message(message).build())
                .build(),
            new AddEntryProjectionRoot().message());

    var response =
        dgsQueryExecutor.executeAndExtractJsonPath(request.serialize(), "data.addEntry.message");

    assertThat(response).isEqualTo(message);
  }
}
