package lab.mops.core.application.lineitem;

import lab.mops.core.domain.budget.LineItemId;
import lab.mops.core.domain.budget.Spend;

public record PlanSpendCommand(LineItemId lineItemId, Spend spend) {}
