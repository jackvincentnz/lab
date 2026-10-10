package lab.wide.infrastructure;

import java.util.Optional;
import java.util.UUID;
import lab.wide.domain.Strategy;
import org.springframework.data.repository.CrudRepository;

public interface StrategyRepository extends CrudRepository<Strategy, UUID> {

  Optional<Strategy> findFirstByOrderByIdAsc();
}
