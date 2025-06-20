package lab.mops.core.application.lineitem;

import java.util.Collection;
import lab.mops.core.domain.budget.LineItem;
import lab.mops.core.domain.budget.LineItemRepository;
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
