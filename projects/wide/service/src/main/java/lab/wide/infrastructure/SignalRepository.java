package lab.wide.infrastructure;

import java.util.List;
import java.util.UUID;
import lab.wide.domain.Signal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SignalRepository
    extends CrudRepository<Signal, UUID>, PagingAndSortingRepository<Signal, UUID> {

  List<Signal>
      findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrSourceContainingIgnoreCase(
          String title, String content, String source, Pageable pageable);
}
