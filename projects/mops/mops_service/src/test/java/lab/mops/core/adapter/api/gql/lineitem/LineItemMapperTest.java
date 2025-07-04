package lab.mops.core.adapter.api.gql.lineitem;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import lab.mops.core.domain.budget.Budget;
import lab.mops.core.domain.budget.LineItem;
import lab.mops.core.domain.budget.Spend;
import lab.mops.core.domain.category.Category;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class LineItemMapperTest extends TestBase {

  LineItemMapper lineItemMapper = new LineItemMapper();

  @Test
  void map_mapsLineItem_id() {
    var lineItem = newLineItem();

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getId()).isEqualTo(lineItem.getId().toString());
  }

  @Test
  void map_mapsLineItem_budgetId() {
    var lineItem = newLineItem();

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getBudgetId()).isEqualTo(lineItem.getBudgetId().toString());
  }

  @Test
  void map_mapsLineItem_name() {
    var lineItem = newLineItem();

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getName()).isEqualTo(lineItem.getName());
  }

  @Test
  void map_mapsLineItemSpend_spendDay() {
    var spendDay = LocalDate.now();
    var spend = Spend.of(spendDay, new BigDecimal(100));
    var lineItem = newLineItem();
    lineItem.planSpend(spend);

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getSpending().get(0).getDay()).isEqualTo(spendDay);
  }

  @Test
  void map_mapsLineItemSpend_amount() {
    var amount = BigDecimal.valueOf(123.456);
    var spend = Spend.of(LocalDate.now(), amount);
    var lineItem = newLineItem();
    lineItem.planSpend(spend);

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getSpending().get(0).getAmount()).isEqualTo(amount);
  }

  @Test
  void map_mapsLineItemCategorization_categoryId() {
    var lineItem = newLineItem();
    var category = randomCategory();
    var categoryValue = category.getValues().stream().toList().get(0);
    lineItem.categorize(category, categoryValue);

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getCategorizations().get(0).getCategory().getId())
        .isEqualTo(category.getId().toString());
  }

  @Test
  void map_mapsLineItemCategorization_categoryValueId() {
    var lineItem = newLineItem();
    var category = randomCategory();
    var categoryValue = category.getValues().stream().toList().get(0);
    lineItem.categorize(category, categoryValue);

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getCategorizations().get(0).getCategoryValue().getId())
        .isEqualTo(categoryValue.getId().toString());
  }

  private Category randomCategory() {
    var category = Category.create(randomString());
    category.addValue(randomString());
    return category;
  }

  private LineItem newLineItem() {
    var budget = Budget.create(randomString());
    return budget.addLineItem(randomString());
  }
}
