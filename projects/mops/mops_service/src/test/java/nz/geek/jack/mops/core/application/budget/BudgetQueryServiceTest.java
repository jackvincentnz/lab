package nz.geek.jack.mops.core.application.budget;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nz.geek.jack.mops.core.domain.budget.Budget;
import nz.geek.jack.mops.core.domain.budget.BudgetRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetQueryServiceTest extends TestBase {

  @Mock BudgetRepository budgetRepository;

  @InjectMocks BudgetQueryService budgetQueryService;

  @Test
  void findAll_delegatesToRepository() {
    var budgets = List.of(Budget.create(randomString()));
    when(budgetRepository.findAll()).thenReturn(budgets);

    budgetQueryService.findAll();

    verify(budgetRepository).findAll();
  }
}
