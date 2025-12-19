package lab.mops.core.application.category;

import lab.mops.core.domain.category.CategoryId;

public record AddCategoryValueCommand(CategoryId categoryId, String name) {}
