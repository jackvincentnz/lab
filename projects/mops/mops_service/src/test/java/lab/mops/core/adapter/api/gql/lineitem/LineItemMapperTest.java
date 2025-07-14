package lab.mops.core.adapter.api.gql.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import lab.mops.api.gql.types.AnnualTotal;
import lab.mops.api.gql.types.Month;
import lab.mops.api.gql.types.MonthlyTotal;
import lab.mops.api.gql.types.Quarter;
import lab.mops.api.gql.types.QuarterlyTotal;
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

  @Test
  void map_mapsSpendTotals_grandTotal() {
    var lineItem = newLineItem();
    lineItem.planSpend(
        Spend.of(LocalDate.of(2025, java.time.Month.JANUARY, 1), BigDecimal.valueOf(100)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2025, java.time.Month.FEBRUARY, 1), BigDecimal.valueOf(200)));

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getSpendTotals().getGrandTotal()).isEqualTo(BigDecimal.valueOf(300));
  }

  @Test
  void map_mapsSpendTotals_monthlyTotals() {
    var lineItem = newLineItem();
    lineItem.planSpend(
        Spend.of(LocalDate.of(2024, java.time.Month.JANUARY, 1), BigDecimal.valueOf(100)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2024, java.time.Month.JANUARY, 15), BigDecimal.valueOf(50)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2025, java.time.Month.JANUARY, 1), BigDecimal.valueOf(200)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2025, java.time.Month.FEBRUARY, 1), BigDecimal.valueOf(300)));

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getSpendTotals().getMonthlyTotals())
        .extracting(MonthlyTotal::getMonth, MonthlyTotal::getYear, MonthlyTotal::getTotal)
        .containsExactly(
            tuple(Month.JANUARY, 2024, BigDecimal.valueOf(150)),
            tuple(Month.JANUARY, 2025, BigDecimal.valueOf(200)),
            tuple(Month.FEBRUARY, 2025, BigDecimal.valueOf(300)));
  }

  @Test
  void map_mapsSpendTotals_quarterlyTotals() {
    var lineItem = newLineItem();

    lineItem.planSpend(
        Spend.of(LocalDate.of(2024, java.time.Month.JANUARY, 1), BigDecimal.valueOf(100)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2024, java.time.Month.FEBRUARY, 1), BigDecimal.valueOf(50)));

    lineItem.planSpend(
        Spend.of(LocalDate.of(2025, java.time.Month.JANUARY, 1), BigDecimal.valueOf(200)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2025, java.time.Month.MARCH, 1), BigDecimal.valueOf(100)));

    lineItem.planSpend(
        Spend.of(LocalDate.of(2025, java.time.Month.APRIL, 1), BigDecimal.valueOf(300)));

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getSpendTotals().getQuarterlyTotals())
        .extracting(
            QuarterlyTotal::getQuarter, QuarterlyTotal::getFiscalYear, QuarterlyTotal::getTotal)
        .containsExactly(
            tuple(Quarter.Q1, 2024, BigDecimal.valueOf(150)),
            tuple(Quarter.Q1, 2025, BigDecimal.valueOf(300)),
            tuple(Quarter.Q2, 2025, BigDecimal.valueOf(300)));
  }

  @Test
  void map_mapsSpendTotals_annualTotals() {
    var lineItem = newLineItem();
    lineItem.planSpend(
        Spend.of(LocalDate.of(2024, java.time.Month.JANUARY, 1), BigDecimal.valueOf(100)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2024, java.time.Month.DECEMBER, 31), BigDecimal.valueOf(200)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2025, java.time.Month.JANUARY, 1), BigDecimal.valueOf(300)));
    lineItem.planSpend(
        Spend.of(LocalDate.of(2026, java.time.Month.JANUARY, 1), BigDecimal.valueOf(400)));

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getSpendTotals().getAnnualTotals())
        .extracting(AnnualTotal::getYear, AnnualTotal::getTotal)
        .containsExactly(
            tuple(2024, BigDecimal.valueOf(300)),
            tuple(2025, BigDecimal.valueOf(300)),
            tuple(2026, BigDecimal.valueOf(400)));
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
