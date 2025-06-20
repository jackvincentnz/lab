package nz.geek.jack.mops.core.application.lineitem;

import nz.geek.jack.mops.core.domain.budget.LineItemId;
import nz.geek.jack.mops.core.domain.budget.Spend;

public record PlanSpendCommand(LineItemId lineItemId, Spend spend) {}
