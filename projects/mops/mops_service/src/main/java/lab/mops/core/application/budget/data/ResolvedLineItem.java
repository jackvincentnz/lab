package lab.mops.core.application.budget.data;

import java.util.Collection;

public record ResolvedLineItem(
    String id,
    String name,
    Collection<ResolvedCategorization> categorizations,
    Collection<Spend> spending) {}
