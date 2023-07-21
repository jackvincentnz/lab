package nz.geek.jack.journal;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class EntriesDataFetcher {

  private final List<Entry> entries =
      List.of(
          new Entry("1", "Stranger Things", "2016"),
          new Entry("2", "Ozark", "2017"),
          new Entry("3", "The Crown", "2016"),
          new Entry("4", "Dead to Me", "2019"),
          new Entry("5", "Orange is the New Black", "2013"));

  @DgsQuery
  public List<Entry> allEntries(@InputArgument String entryFilter) {
    if (entryFilter == null) {
      return entries;
    }

    return entries.stream()
        .filter(s -> s.getMessage().contains(entryFilter))
        .collect(Collectors.toList());
  }

  @DgsData(parentType = "Entry")
  public Author author(DgsDataFetchingEnvironment dfe) {
    Entry entry = dfe.getSource();
    return new Author("1", String.format("%s Author", entry.getMessage()));
  }
}
