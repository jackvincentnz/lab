package lab.mops.core.application.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.core.domain.budget.Budget;
import lab.mops.core.domain.budget.LineItem;
import lab.mops.core.domain.budget.LineItemRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineItemQueryServiceTest extends TestBase {

  @Mock LineItemRepository lineItemRepository;

  @InjectMocks LineItemQueryService lineItemQueryService;

  @Test
  void findAll_delegatesToRepository() {
    var lineItems = List.of(newLineItem());
    when(lineItemRepository.findAll()).thenReturn(lineItems);

    var result = lineItemQueryService.findAll();

    assertThat(result).isEqualTo(lineItems);
  }

  private LineItem newLineItem() {
    var budget = Budget.create(randomString());
    return budget.addLineItem(randomString());
  }
}
