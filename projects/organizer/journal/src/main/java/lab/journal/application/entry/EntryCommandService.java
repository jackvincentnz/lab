package lab.journal.application.entry;

import lab.journal.domain.Entry;
import lab.journal.domain.EntryId;
import lab.journal.domain.EntryRepository;
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
