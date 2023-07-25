package nz.geek.jack.journal.infrastructure.gql;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.List;
import nz.geek.jack.journal.infrastructure.gql.schema.client.AllEntriesGraphQLQuery;
import nz.geek.jack.journal.infrastructure.gql.schema.client.AllEntriesProjectionRoot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {DgsAutoConfiguration.class, EntriesDataFetcher.class})
class EntriesDataFetcherTest {

  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @Test
  void allEntries_shouldContainHardCodedEntries() {
    List<String> messages =
        dgsQueryExecutor.executeAndExtractJsonPath(
            " { allEntries { message createdAt }}", "data.allEntries[*].message");

    assertThat(messages).contains("Ozark");
  }

  @Test
  public void allEntries_withQueryApi() {
    GraphQLQueryRequest graphQLQueryRequest =
        new GraphQLQueryRequest(
            new AllEntriesGraphQLQuery.Builder().entryFilter("Oz").build(),
            new AllEntriesProjectionRoot().message());

    List<String> messages =
        dgsQueryExecutor.executeAndExtractJsonPath(
            graphQLQueryRequest.serialize(), "data.allEntries[*].message");

    assertThat(messages).containsExactly("Ozark");
  }

  @Test
  void allEntries_shouldContainFilteredEntries_whenFiltered() {
    List<String> messages =
        dgsQueryExecutor.executeAndExtractJsonPath(
            " { allEntries(entryFilter: \"Strange\") { message }}", "data.allEntries[*].message");

    assertThat(messages).size().isEqualTo(1);
  }
}
