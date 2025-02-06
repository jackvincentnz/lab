package nz.geek.jack.mops.core.application.lineitem;

import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.mops.core.domain.lineitem.LineItemRepository;
import org.springframework.stereotype.Service;

@Service
public class LineItemCommandService {

  private final LineItemRepository lineItemRepository;

  public LineItemCommandService(LineItemRepository lineItemRepository) {
    this.lineItemRepository = lineItemRepository;
  }

  public LineItem add(AddLineItemCommand command) {
    var lineItem = LineItem.add(command.name());

    return lineItemRepository.save(lineItem);
  }
}
