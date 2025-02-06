package nz.geek.jack.mops.core.application.lineitem;

import java.util.Collection;
import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.mops.core.domain.lineitem.LineItemRepository;
import org.springframework.stereotype.Service;

@Service
public class LineItemQueryService {

  private final LineItemRepository lineItemRepository;

  public LineItemQueryService(LineItemRepository lineItemRepository) {
    this.lineItemRepository = lineItemRepository;
  }

  public Collection<LineItem> findAll() {
    return lineItemRepository.findAll();
  }
}
