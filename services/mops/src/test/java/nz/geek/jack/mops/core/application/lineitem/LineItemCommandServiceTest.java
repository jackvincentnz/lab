package nz.geek.jack.mops.core.application.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryRepository;
import nz.geek.jack.mops.core.domain.lineitem.Categorization;
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

  @Mock CategoryRepository categoryRepository;

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
  void add_addsLineItemWithCategorization() {
    var category = Category.create(randomString());
    var categoryValue = category.addValue(randomString());
    var categorization = Categorization.of(category.getId(), categoryValue.getId());

    when(categoryRepository.mapById(Set.of(category.getId())))
        .thenReturn(Map.of(category.getId(), category));

    var command =
        new AddLineItemCommand(randomString()).withCategorizations(List.of(categorization));

    lineItemCommandService.add(command);

    verify(lineItemRepository).save(lineItemCaptor.capture());
    assertThat(lineItemCaptor.getValue().getCategorizations()).isEqualTo(Set.of(categorization));
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

  @Test
  void categorize_addsCategorization() {
    var lineItem = LineItem.add(randomString());
    var category = Category.create(randomString());
    var categoryValue = category.addValue(randomString());
    when(lineItemRepository.getById(lineItem.getId())).thenReturn(lineItem);
    when(categoryRepository.getById(category.getId())).thenReturn(category);
    when(lineItemRepository.save(lineItem)).thenReturn(lineItem);

    var result =
        lineItemCommandService.categorize(
            new CategorizeLineItemCommand(
                lineItem.getId(), category.getId(), categoryValue.getId()));

    var categorization = lineItem.getCategorizations().iterator().next();

    assertThat(categorization.getCategoryId()).isEqualTo(category.getId());
    assertThat(categorization.getCategoryValueId()).isEqualTo(categoryValue.getId());
    assertThat(result).isEqualTo(lineItem);
  }
}
