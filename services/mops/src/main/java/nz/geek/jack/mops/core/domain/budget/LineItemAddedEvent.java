package nz.geek.jack.mops.core.domain.budget;

public record LineItemAddedEvent(LineItemId id, BudgetId budgetId, String name) {}
