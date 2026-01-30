package lab.journal.application.entry;

import java.util.Collection;
import lab.journal.domain.Entry;
import lab.journal.domain.EntryId;
import lab.journal.domain.EntryRepository;
import org.springframework.stereotype.Service;

@Service
public class EntryQueryService {

  private final EntryRepository entryRepository;

  public EntryQueryService(EntryRepository entryRepository) {
    this.entryRepository = entryRepository;
  }

  public Collection<Entry> getAllEntries() {
    return entryRepository.findAll();
  }

  public Entry getEntry(EntryId entryId) {
    return entryRepository.findById(entryId).orElseThrow();
  }
}
