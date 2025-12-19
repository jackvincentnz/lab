package lab.mops.core.domain.budget;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;

public final class BudgetId extends AbstractId {

  private BudgetId() {
    super();
  }

  private BudgetId(UUID id) {
    super(id);
  }

  public static BudgetId create() {
    return new BudgetId();
  }

  public static BudgetId fromString(String id) {
    return new BudgetId(UUID.fromString(id));
  }
}
