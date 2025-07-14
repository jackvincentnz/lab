package lab.mops.core.adapter.api.gql.lineitem;

import java.util.Collection;
import java.util.List;
import lab.mops.api.gql.types.AnnualTotal;
import lab.mops.api.gql.types.Categorization;
import lab.mops.api.gql.types.Category;
import lab.mops.api.gql.types.CategoryValue;
import lab.mops.api.gql.types.LineItem;
import lab.mops.api.gql.types.Month;
import lab.mops.api.gql.types.MonthlyTotal;
import lab.mops.api.gql.types.Quarter;
import lab.mops.api.gql.types.QuarterlyTotal;
import lab.mops.api.gql.types.Spend;
import lab.mops.api.gql.types.SpendTotals;
import lab.mops.core.domain.budget.LineItemSpendAnnualTotal;
import lab.mops.core.domain.budget.LineItemSpendMonthlyTotal;
import lab.mops.core.domain.budget.LineItemSpendQuarterlyTotal;
import lab.mops.core.domain.budget.LineItemSpendTotals;
import org.springframework.stereotype.Component;

@Component
public class LineItemMapper {

  public LineItem map(lab.mops.core.domain.budget.LineItem lineItem) {
    return LineItem.newBuilder()
        .id(lineItem.getId().toString())
        .budgetId(lineItem.getBudgetId().toString())
        .name(lineItem.getName())
        .categorizations(
            lineItem.getCategorizations().stream().map(this::mapCategorization).toList())
        .spending(lineItem.getSpending().stream().map(this::mapSpend).toList())
        .spendTotals(mapSpendTotals(lineItem.getSpendTotals()))
        .build();
  }

  private Categorization mapCategorization(
      lab.mops.core.domain.budget.Categorization categorization) {
    return Categorization.newBuilder()
        .category(Category.newBuilder().id(categorization.getCategoryId().toString()).build())
        .categoryValue(
            CategoryValue.newBuilder().id(categorization.getCategoryValueId().toString()).build())
        .build();
  }

  private Spend mapSpend(lab.mops.core.domain.budget.Spend s) {
    return Spend.newBuilder().day(s.getSpendDay()).amount(s.getAmount()).build();
  }

  private SpendTotals mapSpendTotals(LineItemSpendTotals totals) {
    return SpendTotals.newBuilder()
        .monthlyTotals(mapMonthlyTotals(totals.monthlyTotals()))
        .quarterlyTotals(mapQuarterlyTotals(totals.quarterlyTotals()))
        .annualTotals(mapAnnualTotals(totals.annualTotals()))
        .grandTotal(totals.grandTotal())
        .build();
  }

  private List<MonthlyTotal> mapMonthlyTotals(Collection<LineItemSpendMonthlyTotal> monthlyTotals) {
    return monthlyTotals.stream().map(this::mapMonthlyTotal).toList();
  }

  private MonthlyTotal mapMonthlyTotal(LineItemSpendMonthlyTotal monthTotal) {
    return MonthlyTotal.newBuilder()
        .month(Month.valueOf(monthTotal.month().name()))
        .year(monthTotal.year().getValue())
        .total(monthTotal.total())
        .build();
  }

  private List<QuarterlyTotal> mapQuarterlyTotals(
      Collection<LineItemSpendQuarterlyTotal> quarterlyTotals) {
    return quarterlyTotals.stream().map(this::mapQuarterlyTotal).toList();
  }

  private QuarterlyTotal mapQuarterlyTotal(LineItemSpendQuarterlyTotal qt) {
    return QuarterlyTotal.newBuilder()
        .quarter(Quarter.valueOf(qt.quarter().name()))
        .fiscalYear(qt.fiscalYear().getValue())
        .total(qt.total())
        .build();
  }

  private List<AnnualTotal> mapAnnualTotals(Collection<LineItemSpendAnnualTotal> annualTotals) {
    return annualTotals.stream()
        .map(at -> AnnualTotal.newBuilder().year(at.year().getValue()).total(at.total()).build())
        .toList();
  }
}
