package nz.geek.jack.journal.infrastructure.gql;

import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {DgsAutoConfiguration.class, EntriesDataFetcher.class})
class EntriesDataFetcherTest {

  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @Test
  void allEntries_shouldContainHardCodedEntries() {
    List<String> titles =
        dgsQueryExecutor.executeAndExtractJsonPath(
            " { allEntries { message createdAt }}", "data.allEntries[*].message");

    assertThat(titles).contains("Ozark");
  }

  @Test
  void allEntries_shouldContainFilteredEntries_whenFiltered() {
    List<String> titles =
        dgsQueryExecutor.executeAndExtractJsonPath(
            " { allEntries(entryFilter: \"Strange\") { message }}", "data.allEntries[*].message");

    assertThat(titles).size().isEqualTo(1);
  }
}
