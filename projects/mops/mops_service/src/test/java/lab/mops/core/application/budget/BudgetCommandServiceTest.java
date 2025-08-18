package lab.mops.core.application.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.core.domain.budget.Budget;
import lab.mops.core.domain.budget.BudgetRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetCommandServiceTest extends TestBase {

  @Mock BudgetRepository budgetRepository;

  @InjectMocks BudgetCommandService budgetCommandService;

  @Captor ArgumentCaptor<Budget> budgetCaptor;

  @Test
  void create_savesBudgetWithName() {
    var command = new CreateBudgetCommand(randomString());

    budgetCommandService.create(command);

    verify(budgetRepository).save(budgetCaptor.capture());
    assertThat(budgetCaptor.getValue().getName()).isEqualTo(command.name());
  }

  @Test
  void create_returnsBudget() {
    var command = new CreateBudgetCommand(randomString());
    var savedBudget = mock(Budget.class);

    when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);

    var result = budgetCommandService.create(command);

    assertThat(result).isEqualTo(savedBudget);
  }
}
