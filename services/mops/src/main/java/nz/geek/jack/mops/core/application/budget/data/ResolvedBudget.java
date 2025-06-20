package nz.geek.jack.mops.core.application.budget.data;

import java.util.Collection;

public record ResolvedBudget(String id, String name, Collection<ResolvedLineItem> lineItems) {}
