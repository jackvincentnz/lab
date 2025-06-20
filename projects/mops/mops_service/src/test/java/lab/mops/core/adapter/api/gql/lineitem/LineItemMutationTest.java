package lab.mops.core.adapter.api.gql.lineitem;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static lab.mops.core.adapter.api.gql.ResponseMessage.CREATED_MESSAGE;
import static lab.mops.core.adapter.api.gql.ResponseMessage.OK_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lab.mops.api.gql.types.AddLineItemInput;
import lab.mops.api.gql.types.CategorizationInput;
import lab.mops.api.gql.types.CategorizeLineItemInput;
import lab.mops.api.gql.types.PlanSpendInput;
import lab.mops.api.gql.types.SpendInput;
import lab.mops.core.application.lineitem.AddLineItemCommand;
import lab.mops.core.application.lineitem.CategorizeLineItemCommand;
import lab.mops.core.application.lineitem.LineItemCommandService;
import lab.mops.core.application.lineitem.PlanSpendCommand;
import lab.mops.core.domain.budget.Categorization;
import lab.mops.core.domain.budget.LineItem;
import lab.mops.core.domain.budget.LineItemId;
import lab.mops.core.domain.budget.Spend;
import lab.mops.core.domain.category.CategoryId;
import lab.mops.core.domain.category.CategoryValueId;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineItemMutationTest extends TestBase {

  @Mock LineItemCommandService lineItemCommandService;

  @Mock LineItemMapper lineItemMapper;

  @InjectMocks LineItemMutation lineItemMutation;

  @Captor ArgumentCaptor<AddLineItemCommand> addLineItemCommandCaptor;

  @Test
  void addLineItem_addsLineItemToBudget() {
    var budgetId = randomId();

    lineItemMutation.addLineItem(
        AddLineItemInput.newBuilder().budgetId(budgetId).name(randomString()).build());

    verify(lineItemCommandService).add(addLineItemCommandCaptor.capture());
    assertThat(addLineItemCommandCaptor.getValue().getBudgetId().toString()).isEqualTo(budgetId);
  }

  @Test
  void addLineItem_addsLineItemWithName() {
    var name = randomString();

    lineItemMutation.addLineItem(
        AddLineItemInput.newBuilder().budgetId(randomId()).name(name).build());

    verify(lineItemCommandService).add(addLineItemCommandCaptor.capture());
    assertThat(addLineItemCommandCaptor.getValue().getName()).isEqualTo(name);
  }

  @Test
  void addLineItem_addsLineItemWithCategorization() {
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();
    var addLineItemInput =
        AddLineItemInput.newBuilder()
            .budgetId(randomId())
            .name(randomString())
            .categorizations(
                List.of(
                    CategorizationInput.newBuilder()
                        .categoryId(categoryId.toString())
                        .categoryValueId(categoryValueId.toString())
                        .build()))
            .build();

    lineItemMutation.addLineItem(addLineItemInput);

    verify(lineItemCommandService).add(addLineItemCommandCaptor.capture());
    assertThat(addLineItemCommandCaptor.getValue().getCategorizations())
        .isEqualTo(Optional.of(List.of(Categorization.of(categoryId, categoryValueId))));
  }

  @Test
  void addLineItem_mapsResponse() {
    var domainLineItem = mock(LineItem.class);
    var graphLineItem = mock(lab.mops.api.gql.types.LineItem.class);

    when(lineItemCommandService.add(any(AddLineItemCommand.class))).thenReturn(domainLineItem);
    when(lineItemMapper.map(domainLineItem)).thenReturn(graphLineItem);

    var result =
        lineItemMutation.addLineItem(
            AddLineItemInput.newBuilder()
                .budgetId(randomId())
                .name(domainLineItem.getName())
                .build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(CREATED_MESSAGE);
    assertThat(result.getLineItem()).isEqualTo(graphLineItem);
  }

  @Test
  void planSpend_plansSpend() {
    var lineItemId = LineItemId.create();
    var spendDay = LocalDate.now();
    var amount = BigDecimal.valueOf(123.456);

    lineItemMutation.planSpend(
        PlanSpendInput.newBuilder()
            .lineItemId(lineItemId.toString())
            .spendInput(SpendInput.newBuilder().spendDay(spendDay).amount(amount).build())
            .build());

    verify(lineItemCommandService)
        .planSpend(new PlanSpendCommand(lineItemId, Spend.of(spendDay, amount)));
  }

  @Test
  void planSpend_mapsResponse() {
    var lineItemId = LineItemId.create();
    var spendDay = LocalDate.now();
    var amount = BigDecimal.valueOf(123.456);
    var domainLineItem = mock(LineItem.class);
    var graphLineItem = mock(lab.mops.api.gql.types.LineItem.class);

    when(lineItemCommandService.planSpend(
            new PlanSpendCommand(lineItemId, Spend.of(spendDay, amount))))
        .thenReturn(domainLineItem);
    when(lineItemMapper.map(domainLineItem)).thenReturn(graphLineItem);

    var result =
        lineItemMutation.planSpend(
            PlanSpendInput.newBuilder()
                .lineItemId(lineItemId.toString())
                .spendInput(SpendInput.newBuilder().spendDay(spendDay).amount(amount).build())
                .build());

    assertThat(result.getCode()).isEqualTo(SC_OK);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(OK_MESSAGE);
    assertThat(result.getLineItem()).isEqualTo(graphLineItem);
  }

  @Test
  public void categorizeLineItem_categorizes() {
    var lineItemId = LineItemId.create();
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();

    lineItemMutation.categorizeLineItem(
        CategorizeLineItemInput.newBuilder()
            .lineItemId(lineItemId.toString())
            .categoryId(categoryId.toString())
            .categoryValueId(categoryValueId.toString())
            .build());

    verify(lineItemCommandService)
        .categorize(new CategorizeLineItemCommand(lineItemId, categoryId, categoryValueId));
  }

  @Test
  public void categorizeLineItem_returnsResponse() {
    var lineItemId = LineItemId.create();
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();
    var domainLineItem = mock(LineItem.class);
    var graphLineItem = mock(lab.mops.api.gql.types.LineItem.class);

    when(lineItemCommandService.categorize(
            new CategorizeLineItemCommand(lineItemId, categoryId, categoryValueId)))
        .thenReturn(domainLineItem);
    when(lineItemMapper.map(domainLineItem)).thenReturn(graphLineItem);

    var result =
        lineItemMutation.categorizeLineItem(
            CategorizeLineItemInput.newBuilder()
                .lineItemId(lineItemId.toString())
                .categoryId(categoryId.toString())
                .categoryValueId(categoryValueId.toString())
                .build());

    assertThat(result.getCode()).isEqualTo(SC_OK);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(OK_MESSAGE);
    assertThat(result.getLineItem()).isEqualTo(graphLineItem);
  }
}
