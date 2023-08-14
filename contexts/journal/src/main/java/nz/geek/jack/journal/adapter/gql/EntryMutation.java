package nz.geek.jack.journal.adapter.gql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import nz.geek.jack.journal.adapter.gql.schema.types.AddEntryInput;
import nz.geek.jack.journal.adapter.gql.schema.types.Entry;
import nz.geek.jack.journal.application.EntryCommandService;
import nz.geek.jack.journal.application.EntryQueryService;

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
