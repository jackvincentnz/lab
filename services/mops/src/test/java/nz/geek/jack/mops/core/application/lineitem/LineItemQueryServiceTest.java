package nz.geek.jack.mops.core.application.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.mops.core.domain.lineitem.LineItemRepository;
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
    var lineItems = List.of(LineItem.add(randomString()));
    when(lineItemRepository.findAll()).thenReturn(lineItems);

    var result = lineItemQueryService.findAll();

    assertThat(result).isEqualTo(lineItems);
  }
}
