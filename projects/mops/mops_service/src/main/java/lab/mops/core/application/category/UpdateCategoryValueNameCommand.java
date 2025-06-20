package lab.mops.core.application.category;

import lab.mops.core.domain.category.CategoryId;
import lab.mops.core.domain.category.CategoryValueId;

public record UpdateCategoryValueNameCommand(
    CategoryId categoryId, CategoryValueId categoryValueId, String name) {}
