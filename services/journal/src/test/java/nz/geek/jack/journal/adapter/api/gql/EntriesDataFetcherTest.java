package nz.geek.jack.journal.adapter.api.gql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.netflix.graphql.dgs.test.EnableDgsTest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.geek.jack.journal.adapter.gql.schema.client.AllEntriesGraphQLQuery;
import nz.geek.jack.journal.adapter.gql.schema.client.AllEntriesProjectionRoot;
import nz.geek.jack.journal.application.entry.EntryQueryService;
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

  private static final List<nz.geek.jack.journal.domain.Entry> ENTRIES =
      Stream.of("1", "2", "3")
          .map(nz.geek.jack.journal.domain.Entry::newEntry)
          .collect(Collectors.toList());

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
    var first = nz.geek.jack.journal.domain.Entry.newEntry("1");
    var second = nz.geek.jack.journal.domain.Entry.newEntry("2");
    var third = nz.geek.jack.journal.domain.Entry.newEntry("3");
    var fourth = nz.geek.jack.journal.domain.Entry.newEntry("4");
    var fifth = nz.geek.jack.journal.domain.Entry.newEntry("5");
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
