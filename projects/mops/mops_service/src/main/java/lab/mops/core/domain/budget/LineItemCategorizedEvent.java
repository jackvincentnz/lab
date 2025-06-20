package lab.mops.core.domain.budget;

import lab.mops.core.domain.category.CategoryId;
import lab.mops.core.domain.category.CategoryValueId;

public record LineItemCategorizedEvent(
    LineItemId id, CategoryId categoryId, CategoryValueId categoryValueId) {}
