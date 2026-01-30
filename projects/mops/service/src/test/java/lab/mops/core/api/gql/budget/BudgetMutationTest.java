package lab.mops.core.api.gql.budget;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static lab.mops.common.api.gql.ResponseMessage.CREATED_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.api.gql.types.CreateBudgetInput;
import lab.mops.core.application.budget.BudgetCommandService;
import lab.mops.core.application.budget.CreateBudgetCommand;
import lab.mops.core.domain.budget.Budget;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetMutationTest extends TestBase {

  @Mock BudgetCommandService budgetCommandService;

  @Mock BudgetMapper budgetMapper;

  @InjectMocks BudgetMutation budgetMutation;

  @Test
  void createBudget_creates() {
    var name = randomString();

    budgetMutation.createBudget(CreateBudgetInput.newBuilder().name(name).build());

    verify(budgetCommandService).create(new CreateBudgetCommand(name));
  }

  @Test
  void createBudget_mapsResponse() {
    var name = randomString();
    var domainBudget = mock(Budget.class);
    var graphBudget = mock(lab.mops.api.gql.types.Budget.class);

    when(budgetCommandService.create(new CreateBudgetCommand(name))).thenReturn(domainBudget);
    when(budgetMapper.map(domainBudget)).thenReturn(graphBudget);

    var result = budgetMutation.createBudget(CreateBudgetInput.newBuilder().name(name).build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(CREATED_MESSAGE);
    assertThat(result.getBudget()).isEqualTo(graphBudget);
  }
}
