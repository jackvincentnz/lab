package lab.wide.infrastructure;

import java.util.List;
import java.util.UUID;
import lab.wide.domain.RefinementDecision;
import org.springframework.data.repository.CrudRepository;

public interface RefinementDecisionRepository extends CrudRepository<RefinementDecision, UUID> {
  List<RefinementDecision> findByProblemIdOrderByCreatedAtAscIdAsc(UUID problemId);

  List<RefinementDecision> findBySolutionIdOrderByCreatedAtAscIdAsc(UUID solutionId);
}
