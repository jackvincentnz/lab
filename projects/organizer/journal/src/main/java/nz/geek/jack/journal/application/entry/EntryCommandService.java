package nz.geek.jack.journal.application.entry;

import nz.geek.jack.journal.domain.Entry;
import nz.geek.jack.journal.domain.EntryId;
import nz.geek.jack.journal.domain.EntryRepository;
import org.springframework.stereotype.Service;

@Service
public class EntryCommandService {

  private final EntryRepository entryRepository;

  public EntryCommandService(EntryRepository entryRepository) {
    this.entryRepository = entryRepository;
  }

  public EntryId addEntry(String message) {
    var entry = Entry.newEntry(message);

    entryRepository.save(entry);

    return entry.getId();
  }
}
