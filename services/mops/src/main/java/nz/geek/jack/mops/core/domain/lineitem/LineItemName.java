package nz.geek.jack.mops.core.domain.lineitem;

import nz.geek.jack.libs.ddd.domain.StringValue;
import nz.geek.jack.libs.ddd.domain.ValidationException;

public final class LineItemName extends StringValue {

  static final int MAXIMUM_LENGTH = 256;

  private LineItemName(String value) {
    super(value);
    validate();
  }

  private void validate() {
    validateNotEmpty();
    validateLength();
  }

  private void validateNotEmpty() {
    if (value.isBlank()) {
      throw new ValidationException("Line item name is blank");
    }
  }

  private void validateLength() {
    if (value.length() > MAXIMUM_LENGTH) {
      throw new ValidationException("Line item name is too long");
    }
  }

  public static LineItemName of(String name) {
    return new LineItemName(name);
  }
}
