package lab.mops.core.domain.budget;

import java.util.Collection;
import nz.geek.jack.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineItemRepository extends BaseRepository<LineItem, LineItemId> {

  Collection<LineItem> findByBudgetId(BudgetId budgetId);
}
