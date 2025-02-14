package nz.geek.jack.mops.core.application.lineitem;

import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValueId;
import nz.geek.jack.mops.core.domain.lineitem.LineItemId;

public record CategorizeLineItemCommand(
    LineItemId lineItemId, CategoryId categoryId, CategoryValueId categoryValueId) {}
