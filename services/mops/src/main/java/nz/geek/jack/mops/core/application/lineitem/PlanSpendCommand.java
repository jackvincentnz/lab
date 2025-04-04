package nz.geek.jack.mops.core.application.lineitem;

import nz.geek.jack.mops.core.domain.lineitem.LineItemId;
import nz.geek.jack.mops.core.domain.lineitem.Spend;

public record PlanSpendCommand(LineItemId lineItemId, Spend spend) {}
