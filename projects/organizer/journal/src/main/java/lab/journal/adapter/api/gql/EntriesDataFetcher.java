package lab.journal.adapter.api.gql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import java.util.Collection;
import java.util.Comparator;
import lab.journal.adapter.gql.schema.types.Author;
import lab.journal.adapter.gql.schema.types.Entry;
import lab.journal.application.entry.EntryQueryService;

@DgsComponent
public class EntriesDataFetcher {

  private final EntryQueryService entryQueryService;

  private final EntryMapper entryMapper;

  public EntriesDataFetcher(EntryQueryService entryQueryService, EntryMapper entryMapper) {
    this.entryQueryService = entryQueryService;
    this.entryMapper = entryMapper;
  }

  @DgsQuery
  public Collection<Entry> allEntries(@InputArgument String entryFilter) {
    var entries = entryQueryService.getAllEntries().stream();

    if (entryFilter != null) {
      entries = entries.filter(s -> s.getMessage().contains(entryFilter));
    }

    return entries
        .sorted(Comparator.comparing(lab.journal.domain.Entry::getCreatedAt))
        .map(entryMapper::map)
        .toList();
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
