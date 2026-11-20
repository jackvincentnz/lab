package lab.wide.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface RefinementQuestionRepository extends CrudRepository<RefinementQuestion, UUID> {
  List<RefinementQuestion> findByProblemIdOrderByCreatedAtAscIdAsc(UUID problemId);

  List<RefinementQuestion> findBySolutionIdOrderByCreatedAtAscIdAsc(UUID solutionId);
}
