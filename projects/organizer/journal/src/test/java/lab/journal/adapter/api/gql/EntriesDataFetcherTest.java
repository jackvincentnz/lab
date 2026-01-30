package lab.journal.adapter.api.gql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.netflix.graphql.dgs.test.EnableDgsTest;
import java.util.List;
import java.util.stream.Stream;
import lab.journal.adapter.gql.schema.client.AllEntriesGraphQLQuery;
import lab.journal.adapter.gql.schema.client.AllEntriesProjectionRoot;
import lab.journal.application.entry.EntryQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {EntriesDataFetcher.class, EntryMapper.class})
@EnableDgsTest
@ExtendWith(MockitoExtension.class)
class EntriesDataFetcherTest {

  @MockitoBean EntryQueryService entryQueryService;
  @Autowired DgsQueryExecutor dgsQueryExecutor;

  private static final List<lab.journal.domain.Entry> ENTRIES =
      Stream.of("1", "2", "3").map(lab.journal.domain.Entry::newEntry).toList();

  @BeforeEach
  void init() {
    when(entryQueryService.getAllEntries()).thenReturn(ENTRIES);
  }

  @Test
  void allEntries_shouldContainAllEntries() {
    List<String> messages =
        dgsQueryExecutor.executeAndExtractJsonPath(
            " { allEntries { message createdAt }}", "data.allEntries[*].message");

    assertThat(messages).contains("1");
    assertThat(messages).contains("2");
    assertThat(messages).contains("3");
  }

  @Test
  void allEntries_shouldContainSortedEntries() {
    var first = lab.journal.domain.Entry.newEntry("1");
    var second = lab.journal.domain.Entry.newEntry("2");
    var third = lab.journal.domain.Entry.newEntry("3");
    var fourth = lab.journal.domain.Entry.newEntry("4");
    var fifth = lab.journal.domain.Entry.newEntry("5");
    when(entryQueryService.getAllEntries())
        .thenReturn(List.of(fifth, first, fourth, third, second));

    List<String> messages =
        dgsQueryExecutor.executeAndExtractJsonPath(
            " { allEntries { message createdAt }}", "data.allEntries[*].message");

    assertThat(first.getMessage()).isEqualTo(messages.get(0));
    assertThat(second.getMessage()).isEqualTo(messages.get(1));
    assertThat(third.getMessage()).isEqualTo(messages.get(2));
    assertThat(fourth.getMessage()).isEqualTo(messages.get(3));
    assertThat(fifth.getMessage()).isEqualTo(messages.get(4));
  }

  @Test
  public void allEntries_withQueryApi() {
    GraphQLQueryRequest graphQLQueryRequest =
        new GraphQLQueryRequest(
            new AllEntriesGraphQLQuery.Builder().entryFilter("2").build(),
            new AllEntriesProjectionRoot().message());

    List<String> messages =
        dgsQueryExecutor.executeAndExtractJsonPath(
            graphQLQueryRequest.serialize(), "data.allEntries[*].message");

    assertThat(messages).containsExactly("2");
  }

  @Test
  void allEntries_shouldContainFilteredEntries_whenFiltered() {
    List<String> messages =
        dgsQueryExecutor.executeAndExtractJsonPath(
            " { allEntries(entryFilter: \"1\") { message }}", "data.allEntries[*].message");

    assertThat(messages).size().isEqualTo(1);
  }
}
