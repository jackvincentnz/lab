package nz.geek.jack.mops.core.application.category;

import nz.geek.jack.mops.core.domain.category.CategoryId;

public record UpdateCategoryNameCommand(CategoryId categoryId, String name) {}
