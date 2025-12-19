package lab.mops.core.application.budget.data;

import java.util.Collection;
import lab.mops.core.domain.budget.LineItemSpendTotals;

public record ResolvedLineItem(
    String id,
    String name,
    Collection<ResolvedCategorization> categorizations,
    Collection<Spend> spending,
    LineItemSpendTotals spendTotals) {}
