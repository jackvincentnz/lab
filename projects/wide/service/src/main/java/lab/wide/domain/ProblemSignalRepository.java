package lab.wide.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface ProblemSignalRepository extends CrudRepository<ProblemSignal, UUID> {
  Optional<ProblemSignal> findByProblemIdAndSignalId(UUID problemId, UUID signalId);

  List<ProblemSignal> findByProblemIdOrderByLinkedAtAscIdAsc(UUID problemId);

  List<ProblemSignal> findBySignalIdOrderByLinkedAtAscIdAsc(UUID signalId);
}
