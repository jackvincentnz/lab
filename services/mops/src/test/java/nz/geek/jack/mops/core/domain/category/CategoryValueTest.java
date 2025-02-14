package nz.geek.jack.mops.core.domain.category;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryValueTest extends TestBase {

  @Test
  void add_setsId() {
    var value = CategoryValue.add(randomString());

    assertThat(value.getId()).isNotNull();
  }

  @Test
  void add_setsName() {
    var name = randomString();

    var value = CategoryValue.add(name);

    assertThat(value.getName()).isEqualTo(name);
  }

  @Test
  void equals_whenNameMatches() {
    var name = randomString();
    var a = CategoryValue.add(name);
    var b = CategoryValue.add(name);

    assertThat(a).isEqualTo(b);
  }

  @Test
  void equals_whenDifferentObject_isFalse() {
    var value = CategoryValue.add(randomString());

    assertThat(value).isNotEqualTo(new Object());
  }

  @Test
  void hashCode_whenNameMatches_isEqual() {
    var name = randomString();
    var a = CategoryValue.add(name);
    var b = CategoryValue.add(name);

    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }
}
