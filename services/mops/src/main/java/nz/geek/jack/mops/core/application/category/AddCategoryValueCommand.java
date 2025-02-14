package nz.geek.jack.mops.core.application.category;

import nz.geek.jack.mops.core.domain.category.CategoryId;

public record AddCategoryValueCommand(CategoryId categoryId, String name) {}
