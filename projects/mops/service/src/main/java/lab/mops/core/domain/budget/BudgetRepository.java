package lab.mops.core.domain.budget;

import lab.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends BaseRepository<Budget, BudgetId> {}
