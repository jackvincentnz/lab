package lab.wide.infrastructure;

import java.util.List;
import java.util.UUID;
import lab.wide.domain.RefinementQuestion;
import org.springframework.data.repository.CrudRepository;

public interface RefinementQuestionRepository extends CrudRepository<RefinementQuestion, UUID> {
  List<RefinementQuestion> findByProblemIdOrderByCreatedAtAscIdAsc(UUID problemId);

  List<RefinementQuestion> findBySolutionIdOrderByCreatedAtAscIdAsc(UUID solutionId);
}
