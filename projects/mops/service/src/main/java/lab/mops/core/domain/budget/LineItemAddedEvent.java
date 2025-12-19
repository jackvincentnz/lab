package lab.mops.core.domain.budget;

public record LineItemAddedEvent(LineItemId id, BudgetId budgetId, String name) {}
