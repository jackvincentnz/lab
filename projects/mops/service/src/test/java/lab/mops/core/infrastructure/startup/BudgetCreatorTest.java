package lab.mops.core.infrastructure.startup;

import static lab.mops.core.infrastructure.startup.BudgetCreator.DEFAULT_BUDGET_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.core.application.budget.BudgetCommandService;
import lab.mops.core.application.budget.BudgetQueryService;
import lab.mops.core.application.budget.CreateBudgetCommand;
import lab.mops.core.domain.budget.Budget;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetCreatorTest extends TestBase {

  @Mock BudgetQueryService budgetQueryService;

  @Mock BudgetCommandService budgetCommandService;

  @InjectMocks BudgetCreator budgetCreator;

  @Test
  void onApplicationStarted_createsDefaultBudgetIfNotPresent() {
    when(budgetQueryService.findAll()).thenReturn(List.of());

    budgetCreator.onApplicationStarted();

    verify(budgetCommandService).create(new CreateBudgetCommand(DEFAULT_BUDGET_NAME));
  }

  @Test
  void onApplicationStarted_wontCreatesDefaultBudgetIfPresent() {
    when(budgetQueryService.findAll()).thenReturn(List.of(Budget.create(DEFAULT_BUDGET_NAME)));

    budgetCreator.onApplicationStarted();

    verify(budgetCommandService, never()).create(any());
  }
}
