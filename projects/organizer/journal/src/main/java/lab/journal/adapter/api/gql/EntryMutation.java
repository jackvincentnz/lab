package lab.journal.adapter.api.gql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lab.journal.adapter.gql.schema.types.AddEntryInput;
import lab.journal.adapter.gql.schema.types.Entry;
import lab.journal.application.entry.EntryCommandService;
import lab.journal.application.entry.EntryQueryService;

@DgsComponent
public class EntryMutation {

  private final EntryCommandService entryCommandService;

  private final EntryQueryService entryQueryService;

  private final EntryMapper entryMapper;

  public EntryMutation(
      EntryCommandService entryCommandService,
      EntryQueryService entryQueryService,
      EntryMapper entryMapper) {
    this.entryCommandService = entryCommandService;
    this.entryQueryService = entryQueryService;
    this.entryMapper = entryMapper;
  }

  @DgsMutation
  public Entry addEntry(@InputArgument("input") AddEntryInput addEntryInput) {
    var entryId = entryCommandService.addEntry(addEntryInput.getMessage());

    var entry = entryQueryService.getEntry(entryId);

    return entryMapper.map(entry);
  }
}
