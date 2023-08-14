package nz.geek.jack.journal.adapter.gql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.geek.jack.journal.adapter.gql.schema.client.AllEntriesGraphQLQuery;
import nz.geek.jack.journal.adapter.gql.schema.client.AllEntriesProjectionRoot;
import nz.geek.jack.journal.application.EntryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {DgsAutoConfiguration.class, EntriesDataFetcher.class, EntryMapper.class})
@ExtendWith(MockitoExtension.class)
class EntriesDataFetcherTest {

  @MockBean EntryQueryService entryQueryService;
  @Autowired DgsQueryExecutor dgsQueryExecutor;

  private static final List<nz.geek.jack.journal.domain.Entry> ENTRIES =
      Stream.of("Stranger Things", "Ozark")
          .map(nz.geek.jack.journal.domain.Entry::newEntry)
          .collect(Collectors.toList());

  @BeforeEach
  void init() {
    when(entryQueryService.getAllEntries()).thenReturn(ENTRIES);
  }

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
