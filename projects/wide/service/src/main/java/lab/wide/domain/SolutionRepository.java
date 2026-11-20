package lab.wide.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SolutionRepository
    extends CrudRepository<Solution, UUID>, PagingAndSortingRepository<Solution, UUID> {

  // Writes only the timestamp, so a concurrent edit of the other fields is not overwritten.
  @Modifying
  @Query("UPDATE solution SET updated_at = :updatedAt WHERE id = :id")
  void touch(UUID id, Instant updatedAt);
}
