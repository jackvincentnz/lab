package nz.geek.jack.mops.core.application.lineitem;

import nz.geek.jack.mops.core.domain.budget.LineItemId;
import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValueId;

public record CategorizeLineItemCommand(
    LineItemId lineItemId, CategoryId categoryId, CategoryValueId categoryValueId) {}
