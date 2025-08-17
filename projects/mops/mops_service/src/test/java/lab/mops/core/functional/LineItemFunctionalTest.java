package lab.mops.core.functional;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import lab.mops.api.gql.client.AllLineItemsGraphQLQuery;
import lab.mops.api.gql.client.AllLineItemsProjectionRoot;
import lab.mops.api.gql.types.CategorizationInput;
import lab.mops.api.gql.types.LineItem;
import lab.mops.api.gql.types.Quarter;
import lab.mops.api.gql.types.SpendInput;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LineItemFunctionalTest extends TestBase {

  @Autowired DgsQueryExecutor dgsQueryExecutor;

  @Autowired TestClient client;

  @Test
  void addLineItemWithAllFields() {
    var budgetId = client.createBudget(randomString()).getBudget().getId();
    var name = randomString();
    var categoryId = client.createCategory(randomString()).getCategory().getId();
    var categoryValueId =
        client.addCategoryValue(categoryId, randomString()).getCategoryValue().getId();

    var response =
        client.addLineItem(
            budgetId,
            name,
            List.of(
                CategorizationInput.newBuilder()
                    .categoryId(categoryId)
                    .categoryValueId(categoryValueId)
                    .build()));

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_CREATED);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getLineItem().getBudgetId()).isEqualTo(budgetId);
    assertThat(response.getLineItem().getName()).isEqualTo(name);

    var categorization = response.getLineItem().getCategorizations().get(0);

    assertThat(categorization.getCategory().getId()).isEqualTo(categoryId);
    assertThat(categorization.getCategoryValue().getId()).isEqualTo(categoryValueId);
  }

  @Test
  void planSpend_plansSpend() {
    var spendDay = LocalDate.now();
    var amount = BigDecimal.valueOf(123.45);

    var lineItemId = newLineItem();

    var response =
        client.planSpend(
            lineItemId, SpendInput.newBuilder().spendDay(spendDay).amount(amount).build());

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getLineItem().getSpending().size()).isEqualTo(1);

    var spend = response.getLineItem().getSpending().get(0);
    assertThat(spend.getDay()).isEqualTo(spendDay);
    assertThat(spend.getAmount()).isEqualTo(amount);
  }

  @Test
  void planSpend_returnsSpendTotals() {
    var spendDay = LocalDate.of(2025, Month.JANUARY, 1);
    var amount = BigDecimal.valueOf(123.45);

    var lineItemId = newLineItem();

    var response =
        client.planSpend(
            lineItemId, SpendInput.newBuilder().spendDay(spendDay).amount(amount).build());

    var spendTotals = response.getLineItem().getSpendTotals();

    assertThat(spendTotals.getMonthlyTotals().size()).isEqualTo(1);
    var monthlyTotal = spendTotals.getMonthlyTotals().get(0);
    assertThat(monthlyTotal.getMonth().name()).isEqualTo(spendDay.getMonth().name());
    assertThat(monthlyTotal.getYear()).isEqualTo(spendDay.getYear());
    assertThat(monthlyTotal.getTotal()).isEqualTo(amount);

    assertThat(spendTotals.getQuarterlyTotals().size()).isEqualTo(1);
    var quarterlyTotal = spendTotals.getQuarterlyTotals().get(0);
    assertThat(quarterlyTotal.getFiscalYear()).isEqualTo(spendDay.getYear());
    assertThat(quarterlyTotal.getQuarter()).isEqualTo(Quarter.Q1);
    assertThat(quarterlyTotal.getTotal()).isEqualTo(amount);

    assertThat(spendTotals.getAnnualTotals().size()).isEqualTo(1);
    var annualTotals = spendTotals.getAnnualTotals().get(0);
    assertThat(annualTotals.getYear()).isEqualTo(spendDay.getYear());
    assertThat(annualTotals.getTotal()).isEqualTo(amount);

    assertThat(spendTotals.getGrandTotal()).isEqualTo(amount);
  }

  @Test
  void categorizeLineItemReturnsAllFields() {
    var lineItemId = newLineItem();
    var categoryId = client.createCategory(randomString()).getCategory().getId();
    var categoryValueId =
        client.addCategoryValue(categoryId, randomString()).getCategoryValue().getId();

    var response = client.categorizeLineItem(lineItemId, categoryId, categoryValueId);

    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getCode()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getMessage()).isNotBlank();
    assertThat(response.getLineItem()).isNotNull();
  }

  @Test
  void allLineItemsReturnsAllFields() {
    var lineItemId = newLineItem();
    var category = client.createCategory(randomString()).getCategory();
    var categoryValue =
        client.addCategoryValue(category.getId(), randomString()).getCategoryValue();
    client.categorizeLineItem(lineItemId, category.getId(), categoryValue.getId());

    var request =
        new GraphQLQueryRequest(
            AllLineItemsGraphQLQuery.newRequest().build(),
            new AllLineItemsProjectionRoot<>()
                .id()
                .name()
                .createdAt()
                .updatedAt()
                .categorizations()
                .category()
                .id()
                .name()
                .parent()
                .categoryValue()
                .id()
                .name());

    var result =
        dgsQueryExecutor.executeAndExtractJsonPathAsObject(
            request.serialize(), "data.allLineItems[0]", LineItem.class);

    assertThat(result.getId()).isNotBlank();
    assertThat(result.getName()).isNotBlank();
    assertThat(result.getCreatedAt()).isNotBlank();
    assertThat(result.getUpdatedAt()).isNotBlank();
    var categorization = result.getCategorizations().get(0);
    assertThat(categorization.getCategory().getId()).isEqualTo(category.getId());
    assertThat(categorization.getCategory().getName()).isEqualTo(category.getName());
    assertThat(categorization.getCategoryValue().getId()).isEqualTo(categoryValue.getId());
    assertThat(categorization.getCategoryValue().getName()).isEqualTo(categoryValue.getName());
  }

  @Test
  void allLineItemsReturnsMultipleItems() {
    var lineItem1 = newLineItem();
    var lineItem2 = newLineItem();
    var lineItem3 = newLineItem();

    var addedIds = getAllLineItemIds();

    assertThat(addedIds).hasSize(3);
    addedIds.forEach(id -> assertThat(List.of(lineItem1, lineItem2, lineItem3)).contains(id));
  }

  @Test
  void deleteAllLineItems_deletesAll() {
    newLineItem();
    var addedIds = getAllLineItemIds();

    assertThat(addedIds).hasSize(1);

    client.deleteAllLineItems();

    var resultIds = getAllLineItemIds();
    assertThat(resultIds).isEmpty();
  }

  private List<String> getAllLineItemIds() {
    var request =
        new GraphQLQueryRequest(
            AllLineItemsGraphQLQuery.newRequest().build(), new AllLineItemsProjectionRoot<>().id());

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
        request.serialize(), "data.allLineItems[*].id", new TypeRef<>() {});
  }

  private String newLineItem() {
    var budgetId = client.createBudget(randomString()).getBudget().getId();
    return client.addLineItem(budgetId, randomString()).getLineItem().getId();
  }
}
