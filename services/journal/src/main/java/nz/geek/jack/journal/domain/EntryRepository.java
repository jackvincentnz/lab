package nz.geek.jack.journal.domain;

import java.util.Collection;

public interface EntryRepository {

  void saveEntry(Entry entry);

  Collection<Entry> getAllEntries();

  Entry getEntry(EntryId id);
}
