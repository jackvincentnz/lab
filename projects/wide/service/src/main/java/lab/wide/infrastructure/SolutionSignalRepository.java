package lab.wide.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lab.wide.domain.SolutionSignal;
import org.springframework.data.repository.CrudRepository;

public interface SolutionSignalRepository extends CrudRepository<SolutionSignal, UUID> {
  Optional<SolutionSignal> findBySolutionIdAndSignalId(UUID solutionId, UUID signalId);

  List<SolutionSignal> findBySolutionIdOrderByLinkedAtAscIdAsc(UUID solutionId);

  List<SolutionSignal> findBySignalIdOrderByLinkedAtAscIdAsc(UUID signalId);
}
