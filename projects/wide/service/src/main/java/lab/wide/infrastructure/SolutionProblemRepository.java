package lab.wide.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lab.wide.domain.SolutionProblem;
import org.springframework.data.repository.CrudRepository;

public interface SolutionProblemRepository extends CrudRepository<SolutionProblem, UUID> {
  Optional<SolutionProblem> findBySolutionIdAndProblemId(UUID solutionId, UUID problemId);

  List<SolutionProblem> findBySolutionIdOrderByLinkedAtAscIdAsc(UUID solutionId);

  List<SolutionProblem> findByProblemIdOrderByLinkedAtAscIdAsc(UUID problemId);
}
