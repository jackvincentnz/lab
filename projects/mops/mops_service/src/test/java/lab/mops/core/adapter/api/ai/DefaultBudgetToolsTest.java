package lab.mops.core.adapter.api.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.core.application.budget.BudgetCommandService;
import lab.mops.core.application.budget.CreateBudgetCommand;
import lab.mops.core.application.budget.ResolvedBudgetQueryService;
import lab.mops.core.application.budget.data.ResolvedBudget;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultBudgetToolsTest extends TestBase {

  @Mock ResolvedBudgetQueryService budgetQueryService;

  @Mock BudgetCommandService budgetCommandService;

  @InjectMocks DefaultBudgetTools defaultBudgetTools;

  @Test
  void createBudget_delegatesToCommandService() {
    var command = new CreateBudgetCommand(randomString());

    defaultBudgetTools.createBudget(command.name());

    verify(budgetCommandService).create(command);
  }

  @Test
  void getAllBudgets_delegatesToQueryService() {
    var budgets = List.of(mock(ResolvedBudget.class));

    when(budgetQueryService.resolveBudgets()).thenReturn(budgets);

    var result = defaultBudgetTools.getAllBudgets();

    assertThat(result).isSameAs(budgets);
  }
}
