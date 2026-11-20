package lab.wide.infrastructure;

import java.time.Instant;
import java.util.UUID;
import lab.wide.domain.Problem;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProblemRepository
    extends CrudRepository<Problem, UUID>, PagingAndSortingRepository<Problem, UUID> {

  // Writes only the timestamp, so a concurrent edit of the other fields is not overwritten.
  @Modifying
  @Query("UPDATE problem SET updated_at = :updatedAt WHERE id = :id")
  void touch(UUID id, Instant updatedAt);
}
