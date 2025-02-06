package nz.geek.jack.mops.core.domain.lineitem;

import static nz.geek.jack.mops.core.domain.lineitem.LineItemName.MAXIMUM_LENGTH;
import static org.junit.jupiter.api.Assertions.assertThrows;

import nz.geek.jack.libs.ddd.domain.ValidationException;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class LineItemNameTest extends TestBase {

  @Test
  void of_validNameSucceeds() {
    var valid = randomString(MAXIMUM_LENGTH);

    LineItemName.of(valid);
  }

  @Test
  void of_nameTooLongThrows() {
    var tooLong = randomString(MAXIMUM_LENGTH + 1);

    assertThrows(ValidationException.class, () -> LineItemName.of(tooLong));
  }

  @Test
  void of_nameBlankThrows() {
    assertThrows(ValidationException.class, () -> LineItemName.of(" "));
  }
}
