package nz.geek.jack.mops.core.domain.lineitem;

import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValueId;

public record LineItemCategorizedEvent(
    LineItemId id, CategoryId categoryId, CategoryValueId categoryValueId) {}
