package lab.wide.infrastructure;

import java.util.UUID;
import lab.wide.domain.Solution;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SolutionRepository
    extends CrudRepository<Solution, UUID>, PagingAndSortingRepository<Solution, UUID> {}
