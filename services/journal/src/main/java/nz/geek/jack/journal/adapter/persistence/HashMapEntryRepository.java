package nz.geek.jack.journal.adapter.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nz.geek.jack.journal.domain.Entry;
import nz.geek.jack.journal.domain.EntryId;
import nz.geek.jack.journal.domain.EntryRepository;
import org.springframework.stereotype.Repository;

@Repository
public class HashMapEntryRepository implements EntryRepository {

  private static final Map<EntryId, Entry> ENTRIES = new ConcurrentHashMap<>();

  @Override
  public void saveEntry(Entry entry) {
    ENTRIES.put(entry.getId(), entry);
  }

  @Override
  public Collection<Entry> getAllEntries() {
    return ENTRIES.values();
  }

  @Override
  public Entry getEntry(EntryId id) {
    if (ENTRIES.containsKey(id)) {
      return ENTRIES.get(id);
    }
    throw new RuntimeException(String.format("Entry: %s does not exist", id));
  }
}
