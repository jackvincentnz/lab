package lab.mops.core.application.lineitem;

import lab.mops.core.domain.budget.LineItemId;
import lab.mops.core.domain.category.CategoryId;
import lab.mops.core.domain.category.CategoryValueId;

public record CategorizeLineItemCommand(
    LineItemId lineItemId, CategoryId categoryId, CategoryValueId categoryValueId) {}
