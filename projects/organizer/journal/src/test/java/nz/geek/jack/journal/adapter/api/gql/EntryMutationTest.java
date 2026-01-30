package nz.geek.jack.journal.adapter.api.gql;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.netflix.graphql.dgs.test.EnableDgsTest;
import nz.geek.jack.journal.adapter.gql.schema.client.AddEntryGraphQLQuery;
import nz.geek.jack.journal.adapter.gql.schema.client.AddEntryProjectionRoot;
import nz.geek.jack.journal.adapter.gql.schema.types.AddEntryInput;
import nz.geek.jack.journal.application.entry.EntryCommandService;
import nz.geek.jack.journal.application.entry.EntryQueryService;
import nz.geek.jack.journal.config.DatabaseConfig;
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
