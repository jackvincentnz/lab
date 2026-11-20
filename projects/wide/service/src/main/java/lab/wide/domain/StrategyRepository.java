package lab.wide.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface StrategyRepository extends CrudRepository<Strategy, UUID> {

  Optional<Strategy> findFirstByOrderByIdAsc();
}
