package nz.geek.jack.mops.core.application.category;

import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValueId;

public record UpdateCategoryValueNameCommand(
    CategoryId categoryId, CategoryValueId categoryValueId, String name) {}
