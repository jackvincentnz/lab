package nz.geek.jack.journal.application.entry;

import java.util.Collection;
import nz.geek.jack.journal.domain.Entry;
import nz.geek.jack.journal.domain.EntryId;
import nz.geek.jack.journal.domain.EntryRepository;
import org.springframework.stereotype.Service;

@Service
public class EntryQueryService {

  private final EntryRepository entryRepository;

  public EntryQueryService(EntryRepository entryRepository) {
    this.entryRepository = entryRepository;
  }

  public Collection<Entry> getAllEntries() {
    return entryRepository.getAllEntries();
  }

  public Entry getEntry(EntryId entryId) {
    return entryRepository.getEntry(entryId);
  }
}
