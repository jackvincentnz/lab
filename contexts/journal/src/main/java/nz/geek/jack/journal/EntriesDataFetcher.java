package nz.geek.jack.journal;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
public class EntriesDataFetcher {

  private final List<Entry> entries =
      List.of(
          new Entry("Stranger Things", "2016"),
          new Entry("Ozark", "2017"),
          new Entry("The Crown", "2016"),
          new Entry("Dead to Me", "2019"),
          new Entry("Orange is the New Black", "2013"));

  @DgsQuery
  public List<Entry> allEntries(@InputArgument String entryFilter) {
    if (entryFilter == null) {
      return entries;
    }

    return entries.stream()
        .filter(s -> s.getMessage().contains(entryFilter))
        .collect(Collectors.toList());
  }
}
