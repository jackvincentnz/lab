package lab.journal.domain;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntryRepository extends CrudRepository<Entry, EntryId> {

  @Override
  Collection<Entry> findAll();
}
