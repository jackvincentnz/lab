package nz.geek.jack.mops.core.domain.lineitem;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class LineItemTest extends TestBase {

  @Test
  void add_setsId() {
    var lineItem = LineItem.add(randomString());

    assertThat(lineItem.getId()).isNotNull();
  }

  @Test
  void add_registersEventWithId() {
    var lineItem = LineItem.add(randomString());

    var event = (LineItemAddedEvent) lineItem.domainEvents().iterator().next();
    assertThat(event.id()).isEqualTo(lineItem.getId());
  }

  @Test
  void add_setsName() {
    var name = randomString();

    var lineItem = LineItem.add(name);

    assertThat(lineItem.getName()).isEqualTo(name);
  }

  @Test
  void add_registersEventWithName() {
    var name = randomString();

    var lineItem = LineItem.add(name);

    var event = (LineItemAddedEvent) lineItem.domainEvents().iterator().next();
    assertThat(event.name()).isEqualTo(lineItem.getName());
  }
}
