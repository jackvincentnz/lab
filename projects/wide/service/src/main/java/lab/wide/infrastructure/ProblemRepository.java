package lab.wide.infrastructure;

import java.util.UUID;
import lab.wide.domain.Problem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ProblemRepository
    extends CrudRepository<Problem, UUID>, PagingAndSortingRepository<Problem, UUID> {}
