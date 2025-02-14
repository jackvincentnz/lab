package nz.geek.jack.mops.core.application.lineitem;

import nz.geek.jack.mops.core.domain.category.CategoryRepository;
import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.mops.core.domain.lineitem.LineItemRepository;
import org.springframework.stereotype.Service;

@Service
public class LineItemCommandService {

  private final LineItemRepository lineItemRepository;

  private final CategoryRepository categoryRepository;

  public LineItemCommandService(
      LineItemRepository lineItemRepository, CategoryRepository categoryRepository) {
    this.lineItemRepository = lineItemRepository;
    this.categoryRepository = categoryRepository;
  }

  public LineItem add(AddLineItemCommand command) {
    var lineItem = LineItem.add(command.name());

    return lineItemRepository.save(lineItem);
  }

  public LineItem categorize(CategorizeLineItemCommand command) {
    var lineItem = lineItemRepository.getById(command.lineItemId());
    var category = categoryRepository.getById(command.categoryId());
    var categoryValue = category.getValue(command.categoryValueId());

    lineItem.categorize(category, categoryValue);

    return lineItemRepository.save(lineItem);
  }
}
