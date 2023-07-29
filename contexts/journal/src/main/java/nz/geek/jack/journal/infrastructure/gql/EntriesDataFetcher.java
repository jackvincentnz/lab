package nz.geek.jack.journal.infrastructure.gql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import java.util.Collection;
import java.util.stream.Collectors;
import nz.geek.jack.journal.application.EntryQueryService;
import nz.geek.jack.journal.infrastructure.gql.schema.types.Author;
import nz.geek.jack.journal.infrastructure.gql.schema.types.Entry;

@DgsComponent
public class EntriesDataFetcher {

  private final EntryQueryService entryQueryService;

  public EntriesDataFetcher(EntryQueryService entryQueryService) {
    this.entryQueryService = entryQueryService;
  }

  @DgsQuery
  public Collection<Entry> allEntries(@InputArgument String entryFilter) {
    var entries = entryQueryService.getAllEntries().stream();

    if (entryFilter == null) {
      return entries.map(this::map).collect(Collectors.toList());
    }

    return entries
        .filter(s -> s.getMessage().contains(entryFilter))
        .map(this::map)
        .collect(Collectors.toList());
  }

  private Entry map(nz.geek.jack.journal.domain.Entry entry) {
    return new Entry(
        entry.getId().toString(), entry.getMessage(), entry.getCreatedAt().toString(), null);
  }

  @DgsData(parentType = "Entry")
  public Author author(DgsDataFetchingEnvironment dfe) throws InterruptedException {
    Entry entry = dfe.getSource();

    // Incur time to load to show how we would split out loading of a slow field from the rest of
    // the type
    Thread.sleep(1000);

    return new Author("1", String.format("%s Author", entry.getMessage()));
  }
}
