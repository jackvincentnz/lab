package lab.mops.core.application.category;

import lab.mops.core.domain.category.CategoryId;

public record UpdateCategoryNameCommand(CategoryId categoryId, String name) {}
