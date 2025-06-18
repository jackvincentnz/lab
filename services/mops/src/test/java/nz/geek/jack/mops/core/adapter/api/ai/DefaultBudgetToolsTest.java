package nz.geek.jack.mops.core.adapter.api.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nz.geek.jack.mops.core.application.budget.BudgetCommandService;
import nz.geek.jack.mops.core.application.budget.BudgetQueryService;
import nz.geek.jack.mops.core.application.budget.CreateBudgetCommand;
import nz.geek.jack.mops.core.domain.budget.Budget;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultBudgetToolsTest extends TestBase {

  @Mock AiBudgetMapper budgetMapper;

  @Mock BudgetQueryService budgetQueryService;

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
    var domainBudget = mock(Budget.class);
    var aiBudget = mock(nz.geek.jack.mops.core.api.ai.Budget.class);
    when(budgetQueryService.findAll()).thenReturn(List.of(domainBudget));
    when(budgetMapper.map(domainBudget)).thenReturn(aiBudget);

    var result = defaultBudgetTools.getAllBudgets();

    assertThat(result).isEqualTo(List.of(aiBudget));
  }
}
