package lab.mops.core.application.lineitem;

import java.util.stream.Collectors;
import lab.mops.core.domain.budget.BudgetRepository;
import lab.mops.core.domain.budget.Categorization;
import lab.mops.core.domain.budget.LineItem;
import lab.mops.core.domain.budget.LineItemRepository;
import lab.mops.core.domain.category.CategoryRepository;
import nz.geek.jack.libs.ddd.domain.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LineItemCommandService {

  private final BudgetRepository budgetRepository;

  private final LineItemRepository lineItemRepository;

  private final CategoryRepository categoryRepository;

  public LineItemCommandService(
      BudgetRepository budgetRepository,
      LineItemRepository lineItemRepository,
      CategoryRepository categoryRepository) {
    this.budgetRepository = budgetRepository;
    this.lineItemRepository = lineItemRepository;
    this.categoryRepository = categoryRepository;
  }

  public LineItem add(AddLineItemCommand command) {
    var budget = budgetRepository.getById(command.getBudgetId());

    var lineItem = budget.addLineItem(command.getName());

    command
        .getCategorizations()
        .ifPresent(
            categorizations -> {
              var categoryIds =
                  categorizations.stream()
                      .map(Categorization::getCategoryId)
                      .collect(Collectors.toSet());
              var categoriesById = categoryRepository.mapById(categoryIds);

              categorizations.forEach(
                  categorization -> {
                    var categoryId = categorization.getCategoryId();
                    var category = categoriesById.get(categoryId);

                    if (category == null) {
                      throw new NotFoundException(categoryId);
                    }

                    var categoryValue = category.getValue(categorization.getCategoryValueId());

                    lineItem.categorize(category, categoryValue);
                  });
            });

    return lineItemRepository.save(lineItem);
  }

  public LineItem planSpend(PlanSpendCommand command) {
    var lineItem = lineItemRepository.getById(command.lineItemId());

    lineItem.planSpend(command.spend());

    return lineItemRepository.save(lineItem);
  }

  public LineItem categorize(CategorizeLineItemCommand command) {
    var lineItem = lineItemRepository.getById(command.lineItemId());
    var category = categoryRepository.getById(command.categoryId());
    var categoryValue = category.getValue(command.categoryValueId());

    lineItem.categorize(category, categoryValue);

    return lineItemRepository.save(lineItem);
  }

  public void deleteAll() {
    lineItemRepository.deleteAll();
  }
}
