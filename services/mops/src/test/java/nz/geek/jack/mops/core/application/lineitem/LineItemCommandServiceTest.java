package nz.geek.jack.mops.core.application.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.mops.core.domain.lineitem.LineItemRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineItemCommandServiceTest extends TestBase {

  @Mock LineItemRepository lineItemRepository;

  @InjectMocks LineItemCommandService lineItemCommandService;

  @Captor ArgumentCaptor<LineItem> lineItemCaptor;

  @Test
  void add_addsLineItemWithName() {
    var name = randomString();
    var command = new AddLineItemCommand(name);

    lineItemCommandService.add(command);

    verify(lineItemRepository).save(lineItemCaptor.capture());
    assertThat(lineItemCaptor.getValue().getName()).isEqualTo(name);
  }

  @Test
  void add_returnsSavedLineItem() {
    var name = randomString();
    var command = new AddLineItemCommand(name);
    var saved = LineItem.add(name);

    when(lineItemRepository.save(any(LineItem.class))).thenReturn(saved);

    var result = lineItemCommandService.add(command);

    assertThat(result).isEqualTo(saved);
  }
}
