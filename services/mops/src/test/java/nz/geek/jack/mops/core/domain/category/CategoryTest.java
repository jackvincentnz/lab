package nz.geek.jack.mops.core.domain.category;

import static nz.geek.jack.libs.ddd.domain.test.AggregateTestUtils.getLastEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import nz.geek.jack.libs.ddd.domain.DuplicateException;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class CategoryTest extends TestBase {

  @Test
  void create_setsId() {
    var category = Category.create(randomString());

    assertThat(category.getId()).isNotNull();
  }

  @Test
  void create_registersEventWithId() {
    var category = Category.create(randomString());

    var event = (CategoryCreatedEvent) category.domainEvents().iterator().next();
    assertThat(event.id()).isEqualTo(category.getId());
  }

  @Test
  void create_setsName() {
    var name = randomString();

    var category = Category.create(name);

    assertThat(category.getName()).isEqualTo(name);
  }

  @Test
  void create_registersEventWithName() {
    var name = randomString();

    var category = Category.create(name);

    var event = (CategoryCreatedEvent) category.domainEvents().iterator().next();
    assertThat(event.name()).isEqualTo(name);
  }

  @Test
  void create_hasEmptyValues() {
    var category = Category.create(randomString());

    assertThat(category.getValues()).isEmpty();
  }

  @Test
  void addValue_setsId() {
    var category = Category.create(randomString());

    var value = category.addValue(randomString());

    assertThat(value.getId()).isNotNull();
  }

  @Test
  void addValue_registersEventWithId() {
    var category = Category.create(randomString());

    category.addValue(randomString());

    var event = getLastEvent(category, CategoryValueAddedEvent.class);
    assertThat(event.id()).isNotNull();
  }

  @Test
  void addValue_setsName() {
    var name = randomString();
    var category = Category.create(randomString());

    var value = category.addValue(name);

    assertThat(value.getName()).isEqualTo(name);
  }

  @Test
  void addValue_registersEventWithName() {
    var name = randomString();
    var category = Category.create(randomString());

    category.addValue(name);

    var event = getLastEvent(category, CategoryValueAddedEvent.class);
    assertThat(event.name()).isEqualTo(name);
  }

  @Test
  void addValue_preventsDuplicates() {
    var name = randomString();
    var category = Category.create(randomString());

    category.addValue(name);

    assertThrows(DuplicateException.class, () -> category.addValue(name));
  }

  @Test
  void getValues_returnsValues() {
    var category = Category.create(randomString());

    var value = category.addValue(randomString());

    assertThat(category.getValues()).containsExactly(value);
  }

  @Test
  void getValue_returnsValue() {
    var category = Category.create(randomString());

    var value = category.addValue(randomString());

    assertThat(category.getValue(value.getId())).isEqualTo(value);
  }
}
